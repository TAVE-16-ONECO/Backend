package com.oneco.backend.StudyRecord.application.service;

import static com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult;
import com.oneco.backend.StudyRecord.application.port.in.GetHomeDashboardUseCase;
import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardCategoryReadPort;
import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardDailyContentReadPort;
import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardMissionReadPort;
import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;
import com.oneco.backend.StudyRecord.infrastructure.persistence.StudyRecordJpaRepository;
import com.oneco.backend.category.domain.exception.constant.CategoryErrorCode;
import com.oneco.backend.dailycontent.domain.dailycontent.DailyContentId;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.mission.MissionDateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetHomeDashboardService implements GetHomeDashboardUseCase {

	private final HomeDashboardMissionReadPort homeDashboardMissionReadPort;
	private final HomeDashboardCategoryReadPort homeDashboardCategoryReadPort;
	private final HomeDashboardDailyContentReadPort homeDashboardDailyContentReadPort;
	private final StudyRecordJpaRepository repository;

	// ==========================
	// 서비스 흐름 (HomeDashboard)
	// ==========================
	//
	// 목표:
	// - 홈 화면에서 "대시보드" 구성을 위한 데이터를 한 번에 조회하여 반환한다.
	//   1) 미션/카테고리(키워드) 정보
	//   2) 오늘 학습해야 할 DailyContent 1건
	//   3) 캘린더 뷰(미션 기간의 평일 날짜별 조개 상태를 한 번에 내려준다.
	//
	// 전체 흐름:
	// 1) memberId로 최신 진행중 미션 1건 조회
	//    - 없으면 홈 대시보드 구성이 불가하므로 예외
	//
	// 2) 미션의 categoryId로 카테고리 정보 조회
	//
	// 3) today 기준 "오늘이 미션의 몇 번째 학습일인가(daySequence)" 계산
	//    - 미션 기간: startDate ~ endDate
	//    - 주말은 학습일 계산에서 제외
	//    - 주말(today가 토/일)이라면 직전 금요일 콘텐츠가 선택되도록 계산 로직에 포함
	//    => elapsedDays = 오늘에 해당하는 daySequence (1부터 시작)
	//
	// 4) (categoryId, elapsedDays)로 "오늘의 DailyContent" 1건 조회
	//    - 여기서는 사용자가 했는지 여부보다 "오늘 해야 할 콘텐츠가 무엇인지"가 핵심
	//
	// 5) 캘린더 상태 계산을 위해 StudyRecord 목록 조회
	//    - 조건: (memberId, missionId, categoryId)
	//    - StudyRecord는 dailyContentId + quizProgressStatus를 통해 "학습 상태"를 결정하는 근거
	//
	// 6) records를 dailyContentId 기준으로 요약(Map)한다.
	//    - 이유: 캘린더는 날짜별로 생성되지만, 기록은 콘텐츠별로 존재하므로 빠른 조회(O(1))를 위해 인덱싱
	//    - Map<Long dailyContentId, StudyStatusResult> statusByDailyContentId
	//    - quizProgressStatus -> StudyStatusResult 변환 규칙:
	//        * RETRY_AVAILABLE / PASSED / FAILED  -> COMPLETED(파란조개)
	//        * READY / IN_PROGRESS                -> IN_PROGRESS(하늘조개)
	//    - 같은 dailyContentId 기록이 여러 개일 수 있으므로 merge로 "COMPLETED 우선" 규칙을 보장
	//
	// 7) 캘린더의 날짜 -> dailyContentId 매핑을 위해, 해당 카테고리의 DailyContent 전체를 daySequence 오름차순으로 조회
	//    - dailyContents.get(daySequence - 1) 로 해당 날짜의 콘텐츠를 찾을 수 있게 준비
	//
	// 8) 캘린더 리스트(calendarDateResults) 생성
	//    - startDate ~ endDate 날짜를 하루씩 순회
	//    - 주말(토/일)은 캘린더에서 제외(continue)
	//    - 평일만 daySequence++ 하며 daySequence와 DailyContent를 1:1 매핑
	//    - 각 날짜의 최종 studyStatus 결정 규칙:
	//        * date > today  -> NOT_AVAILABLE(회색조개)  // 미래 날짜
	//        * date <= today -> (Map에 값 있으면 그 값, 없으면 IN_PROGRESS)  // 열린 날짜지만 기록이 없으면 "아직 안 함"
	//
	// 9) 최종 HomeDashboardResult 조립 후 반환
	//    - missionResult / category / todayDailyContent / calendarDateResults 를 한 번에 내려준다.

	@Override
	public HomeDashboardResult getHomeDashboard(Long memberId, Long missionId) {

		MissionResult mission;

		// missionId가 없는경우
		// 1. memberId로 진행중 가장 최신의 미션 1건 조회
		if (missionId == null) {
			mission = homeDashboardMissionReadPort.findLatestActiveMission(memberId)
				.orElseThrow(() -> BaseException.from(MissionErrorCode.MISSION_NOT_FOUND));
		} else {
			// missionId가 있는경우
			// 1. missionId, memberId, 진행중인 미션 1건 조회
			mission = homeDashboardMissionReadPort.findActiveMissionById(memberId, missionId)
				.orElseThrow(() -> BaseException.from(MissionErrorCode.MISSION_NOT_FOUND));
		}

		// 2. 미션의 categoryId로 카테고리 정보(CategoryResult) 조회
		CategoryResult category = homeDashboardCategoryReadPort.findById(mission.categoryId())
			.orElseThrow(() -> BaseException.from(CategoryErrorCode.INVALID_CATEGORY_ID,
				"Invalid categoryId: " + mission.categoryId()));
		log.info("미션의 categoryId로 카테고리 정보(CategoryResult) 조회 완료. categoryId = {}, categoryTitle= {}",
			category.categoryId(), category.categoryTitle());

		// 3. today 기준 "오늘이 미션의 몇 번째 학습일인가(daySequence)" 계산
		LocalDate today = LocalDate.now();
		log.info("오늘 날짜 today = {}", today);

		// elapsedDays(시작한지 몇 번째 날인가) = 오늘에 해당하는 daySequence (1부터 시작)
		log.info("미션 기간: startDate = {}, endDate = {}", mission.startDate(), mission.endDate());

		int elapsedDays = MissionDateCalculator.openedDaySequenceExcludeWeekend(
			mission.startDate(),
			mission.endDate(),
			today
		);
		log.info("오늘이 미션의 몇번째 학습일인가(daySequence) 계산 완료. elapsedDays = {}", elapsedDays);

		// 4. (categoryId, elapsedDays)로 "오늘의 DailyContent" 1건 조회
		DailyContentResult dailyContent = homeDashboardDailyContentReadPort.findByCategoryIdAndDaySequence(
			mission.categoryId(),
			elapsedDays
		);

		// 5. 캘린더 상태 계산을 위해 StudyRecord 목록 조회
		List<StudyRecord> records = repository.findByMemberIdAndMissionIdAndCategoryId(
			memberId,
			mission.missionId(),
			mission.categoryId()
		);

		// 6. records를 dailyContentId 기준으로 요약(Map)한다.
		Map<Long, StudyStatusResult> statusByDailyContentId = new HashMap<>();

		// records 순회하며 dailyContentId 별로 상태 매핑
		for (StudyRecord record : records) {
			DailyContentId dailyContentId = record.getDailyContentId();
			Long dcIdValue = dailyContentId.getValue();
			// quizProgressStatus -> StudyStatusResult 변환
			// RETRY_AVAILABLE / PASSED / FAILED  -> COMPLETED(파란조개)
			// READY / IN_PROGRESS                -> IN_PROGRESS(하늘조개)
			StudyStatusResult mapped = switch (record.getQuizProgressStatus()) {
				case RETRY_AVAILABLE, PASSED, FAILED -> StudyStatusResult.COMPLETED;
				case READY, IN_PROGRESS -> StudyStatusResult.IN_PROGRESS;
			};

			// 동일한 dailyContentId에 대해 여러 개의 StudyRecord가 있을 수 있으므로 "완료가 최우선" 되도록 합치기
			statusByDailyContentId.merge(dcIdValue, mapped, (oldV, newV) -> {
				if (oldV == StudyStatusResult.COMPLETED || newV == StudyStatusResult.COMPLETED) {
					return StudyStatusResult.COMPLETED;
				}
				return StudyStatusResult.IN_PROGRESS;
			});
		}

		// daySequence 오름차순으로 전체 콘텐츠 조회 (calendar 매핑용)
		List<DailyContentResult> dailyContents = homeDashboardDailyContentReadPort
			.findAllByCategoryIdOrderByDaySequence(mission.categoryId());

		// 캘린더 날짜 생성 (주말 제외)
		List<CalendarDateResult> calendarDateResults = buildCalendarDateResults(
			mission,
			today,
			dailyContents,
			statusByDailyContentId
		);

		return new HomeDashboardResult(
			mission, //
			elapsedDays,
			category,
			dailyContent,
			calendarDateResults
		);
	}

	// 캘린더 생성 로직 분리
	private List<CalendarDateResult> buildCalendarDateResults(
		MissionResult missionResult,
		LocalDate today,
		List<DailyContentResult> dailyContents,
		Map<Long, StudyStatusResult> statusByDailyContentId
	) {
		// 캘린더 날짜 생성 (주말 제외)
		List<CalendarDateResult> calendarDateResults = new ArrayList<>();
		LocalDate startDate = missionResult.startDate();
		LocalDate endDate = missionResult.endDate();

		int daySequence = 0; // daySequence는 1부터 시작하므로, 루프 내에서 증가시킴
		// 시작일부터 종료일까지 순회한다. 예를들어 2026-01-01 ~ 2026-01-10이면 10일 동안 순회
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			DayOfWeek dow = date.getDayOfWeek(); // MONDAY ~ SUNDAY
			if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
				continue; // 주말 제외
			}

			daySequence++; // 주말이 아닌 날에만 증가
			Long dailyContentId = null; // 해당 날짜의 일일 콘텐츠 ID
			// daySequence에 해당하는 dailyContentId 조회
			// 예: daySequence가 3이면, dailyContents 리스트에서 인덱스 2의 dailyContentId를 가져옴
			if (dailyContents.size() >= daySequence) {
				// daySequence는 1부터 시작하므로, dailyContentId(인덱스)는 daySequence - 1
				dailyContentId = dailyContents.get(daySequence - 1).dailyContentId();
			}

			StudyStatusResult studyStatus; // 해당 날짜의 학습 상태
			if (date.isAfter(today)) { // 오늘 날짜보다 이후라면
				studyStatus = StudyStatusResult.NOT_AVAILABLE; // 미래 날짜
			} else {
				// dailyContentId가 null인 경우 예: 미션 기간이 콘텐츠 수보다 더 길 때 (비정상) -> mappedStatus도 null 지정
				// mappedStatus가 null인 경우에는 기본값 IN_PROGRESS 지정
				// 왜? 비정상이지만 일단 학습 가능한 상태이므로 IN_PROGRESS(하늘색조개)로 처리
				StudyStatusResult mappedStatus =
					dailyContentId == null ? null : statusByDailyContentId.get(dailyContentId);
				studyStatus = mappedStatus != null ? mappedStatus : StudyStatusResult.IN_PROGRESS;
			}

			// 캘린더에 결과 추가
			calendarDateResults.add(CalendarDateResult.of(date, dailyContentId, studyStatus));
		}
		// 순회 끝~

		return calendarDateResults;
	}
}
