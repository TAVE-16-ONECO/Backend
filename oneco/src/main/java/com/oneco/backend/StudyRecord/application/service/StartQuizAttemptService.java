package com.oneco.backend.StudyRecord.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.command.StartQuizAttemptCommand;
import com.oneco.backend.StudyRecord.application.dto.result.AttemptSummary;
import com.oneco.backend.StudyRecord.application.dto.result.QuizView;
import com.oneco.backend.StudyRecord.application.dto.result.StartQuizAttemptResult;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentWithQuizzesSnapshot;
import com.oneco.backend.StudyRecord.application.port.dto.QuizSnapshot;
import com.oneco.backend.StudyRecord.application.port.in.StartQuizAttemptUseCase;
import com.oneco.backend.StudyRecord.application.port.out.DailyContentQueryPort;
import com.oneco.backend.StudyRecord.application.port.out.QuizQueryPort;
import com.oneco.backend.StudyRecord.application.port.out.StudyRecordPersistencePort;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.StudyRecord.domain.quizAttempt.QuizAttempt;
import com.oneco.backend.StudyRecord.domain.studyRecord.QuizProgressStatus;
import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;
import com.oneco.backend.StudyRecord.infrastructure.persistence.StudyRecordPersistenceAdapter;
import com.oneco.backend.global.exception.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartQuizAttemptService implements StartQuizAttemptUseCase {

	private final StudyRecordPersistenceAdapter studyRecordPersistenceAdapter;
	private final DailyContentQueryPort dailyContentQueryPort;
	private final QuizQueryPort quizQueryPort;
	private final StudyRecordPersistencePort studyRecordPersistencePort;

	@Override
	@Transactional
	public StartQuizAttemptResult start(StartQuizAttemptCommand command, Long memberId) {

		// =========================
		// Step 1) Command에서 studyRecordId 추출
		// =========================
		Long studyRecordId = command.studyRecordId();
		log.info("studyRecordId={}에 대한 퀴즈 시도 시작 요청 받음, memberId={}", studyRecordId, memberId);

		// =========================
		// Step 2) StudyRecord 조회(Attempt까지 함께)
		// =========================
		// - 재시도 흐름에서는 1회차 attempt의 quizIds가 필요하므로 attempts까지 같이 로드한다.
		// - StudyRecord 없으면 "존재하지 않는 학습기록" 예외.
		// - 첫 시도인 경우 Attempts 컬렉션은 비어있다. 비어있어도 뒤에서 객체 생성
		StudyRecord sr = studyRecordPersistenceAdapter.findByIdWithAttempts(studyRecordId)
			.orElseThrow(() -> BaseException.from(StudyErrorCode.STUDY_RECORD_NOT_FOUND));
		log.info("학습기록 조회 완료: {}", sr);

		// =========================
		// Step 3) 소유권 검증
		// =========================
		// - 요청자가 해당 StudyRecord의 소유자인지 검증
		if (!sr.getMemberId().getValue().equals(memberId)) {
			throw BaseException.from(StudyErrorCode.UNAUTHORIZED_ACCESS, "해당 학습기록에 대한 접근 권한이 없습니다.");
		}
		log.info("학습기록 소유권 검증 통과: memberId={}", memberId);

		// =========================
		// Step 4) 현재 퀴즈 진행 상태 확인
		// =========================
		// - READY: 아직 퀴즈를 시작할 수 있는 상태(첫 시도)
		// - RETRY_AVAILABLE: 1회 실패 후 재시도 가능 상태(두 번째 시도)
		// - IN_PROGRESS / PASSED / FAILED 등은 여기서 시작 불가
		QuizProgressStatus currentStatus = sr.getQuizProgressStatus();
		log.info("현재 퀴즈 진행 상태: {}", currentStatus);

		// 1) quizIds: attempt에 넣을 퀴즈 ID 목록
		// 2) quizViews: 응답에 넣어줄 퀴즈 뷰 목록
		final List<Long> quizIds;
		final List<QuizView> quizViews;

		// =========================
		// Step 5) 상태(status)별 퀴즈 데이터 준비
		// =========================
		if (currentStatus == QuizProgressStatus.READY) {
			log.info("첫 시도 상태로 퀴즈 시도 시작 처리 진행");
			// =========================
			// Step 5-1) READY 상태 처리 (첫 시도): DailyContent(+Quizzes) 조회) 로드
			// =========================
			// - DailyContent에서 해당 day의 퀴즈들을 가져와 quizIds를 만듬
			// - 처음 시도이므로 여기서 quizIds 구성
			DailyContentWithQuizzesSnapshot loaded =
				dailyContentQueryPort.loadDailyContentWithQuizzes(sr.getDailyContentId().getValue());
			log.info("DailyContentWithQuizzesSnapshot 조회 완료: {}", loaded);

			// (1) attempt에 넣을 퀴즈 ID 목록 생성
			quizIds = loaded.quizzes().stream()
				.map(QuizSnapshot::quizId)
				.toList();
			log.info("첫 시도용 quizIds 생성 완료: {}", quizIds);

			// (2) 화면에 내려줄 quizViews 생성
			quizViews = loaded.quizzes().stream()
				.map(q -> new QuizView(
					q.quizId(),
					q.question(),
					q.options()
				))
				.toList();
			log.info("첫 시도용 quizViews 생성 완료: {}", quizViews);

		} else if (currentStatus == QuizProgressStatus.RETRY_AVAILABLE) {

			log.info("재시도 가능 상태로 퀴즈 시도 시작 처리 진행");

			// -------------------------
			// Step 5-2) [RETRY_AVAILABLE] 재시도: 1회차 quizIds 재사용
			// -------------------------
			// - 재시도는 동일한 문제 세트로 다시 풀게 하는 정책이므로,
			//   DailyContent에서 다시 로드해서 quizIds를 재생성하면 안 됨.
			//   (콘텐츠가 수정되었거나 정렬이 바뀌면, 재시도 문제 세트가 달라질 수 있음)
			if (sr.getAttempts().isEmpty()) {
				throw BaseException.from(
					StudyErrorCode.INVALID_STUDY_STATUS,
					"재시도 가능한데 1회차 attempt가 없습니다."
				);
			}

			// (1) 1회차 attempt에서 quizIds 재사용
			// - attempts.get(0)은 1회차 시도를 의미
			quizIds = sr.getAttempts().get(0).getQuizIds();
			log.info("재시도용 quizIds 재사용 완료: {}", quizIds);

			// (2) quizIds로 Quiz 상세 재조회 후 화면 구성
			// - 재시도 시작 화면에서도
			quizViews = quizQueryPort.loadQuizzesByIds(quizIds);
			log.info("재시도용 quizViews 재조회 및 생성 완료: {}", quizViews);

		} else {
			// =========================
			// Step 5-3) 그 외 상태 시작 불가
			// =========================
			log.warn("현재 상태에서는 퀴즈 시도를 시작할 수 없음: {}", currentStatus);
			throw BaseException.from(
				StudyErrorCode.INVALID_STUDY_STATUS,
				"현재 퀴즈 시도를 시작할 수 없는 상태입니다. 현재 상태: " + currentStatus
			);
		}
		// =========================
		// Step 6) StudyRecord의 도메인 메서드 호출: 시도 생성 + 상태 변경
		// =========================
		// - 여기서 "QuizAttempt"가 생성되고 StudyRecord에 연결된다.
		// - READY -> IN_PROGRESS, RETRY_AVAILABLE -> IN_PROGRESS 같은 전이가 일어남
		// - 도메인 내부에서:
		//   (a) 상태 검증
		//   (b) attemptNo 계산(1회차/2회차)
		//   (c) attempt 생성 및 연결
		//   (d) quizProgressStatus 변경
		QuizAttempt attempt = sr.startQuizAttempt(quizIds);
		log.info("QuizAttempt 생성 및 StudyRecord에 연결 완료: {}", attempt);

		// =========================
		// Step 7) 저장
		// =========================
		// - aggregate root(sr)를 저장한다.
		// - attempt가 cascade로 걸려있으므로 sr 저장 시 attempt도 함께 저장된다.
		StudyRecord saved = studyRecordPersistencePort.save(sr);
		log.info("StudyRecord 및 연결된 QuizAttempt 저장 완료: {}", saved);

		// =========================
		// Step 8) 응답 계산
		// =========================
		// 1회차 시작 시 remainingAttempts=1, 2회차 시작 시 remainingAttempts=0
		int attemptNo = attempt.getAttemptNo().getValue();
		int remainingAttempts = 2 - attemptNo;
		log.info("응답용 남은 시도 횟수 계산 완료: {}", remainingAttempts);

		QuizAttempt savedAttempt = saved.getAttempts().stream()
			.filter(a -> a.getAttemptNo().getValue() == attemptNo)
			.findFirst()
			.orElseThrow(() -> BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "저장 후 attempt를 찾지 못했습니다."));

		// =========================
		// Step 9) 결과 반환
		// =========================
		return new StartQuizAttemptResult(
			saved.getId(),
			saved.getDailyContentId().getValue(),
			new AttemptSummary(
				savedAttempt.getId(),
				attemptNo,
				savedAttempt.getAttemptStatus()
			),
			saved.getQuizProgressStatus(),
			remainingAttempts,
			quizViews
		);
	}

}
