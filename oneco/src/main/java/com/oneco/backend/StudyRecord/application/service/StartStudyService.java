package com.oneco.backend.StudyRecord.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.command.StartStudyCommand;
import com.oneco.backend.StudyRecord.application.dto.result.StartStudyResult;
import com.oneco.backend.StudyRecord.application.port.dto.ActiveMissionSnapshot;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSnapshot;
import com.oneco.backend.StudyRecord.application.port.in.StartStudyUseCase;
import com.oneco.backend.StudyRecord.application.port.out.DailyContentQueryPort;
import com.oneco.backend.StudyRecord.application.port.out.MissionQueryPort;
import com.oneco.backend.StudyRecord.application.port.out.StudyRecordPersistencePort;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;
import com.oneco.backend.category.domain.category.CategoryId;
import com.oneco.backend.dailycontent.domain.dailycontent.DailyContentId;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.FamilyRole;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.mission.domain.mission.MissionId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartStudyService implements StartStudyUseCase {

	private final DailyContentQueryPort dailyContentQueryPort;
	private final StudyRecordPersistencePort studyRecordPersistencePort;
	private final MissionQueryPort missionQueryPort;

	@Override
	@Transactional
	public StartStudyResult start(StartStudyCommand command, Long memberId, FamilyRole familyRole) {
		Long dailyContentId = command.dailyContentId();

		// 1) DailyContent 로드 (본문/키워드/이미지/+ categoryId/daySequence 포함)
		DailyContentSnapshot dailyContent = dailyContentQueryPort.loadDailyContentSnapshot(dailyContentId);
		log.info("로드된 DailyContentSnapshot: {}", dailyContent);

		//  부모면: 검증만 하고 StudyRecord 생성/조회/저장 없이 바로 반환
		if (familyRole == FamilyRole.PARENT) {
			log.info("부모 요청: StudyRecord 생성 없이 열람 처리. memberId={}, dailyContentId={}", memberId, dailyContentId);

			ActiveMissionSnapshot activeMission =
				missionQueryPort.findActiveMission(memberId, dailyContent.categoryId())
					.orElseThrow(() -> BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "활성 미션이 없습니다."));
			log.info("로드된 ActiveMissionSnapshot: {}", activeMission);

			if (!activeMission.active()) {
				throw BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "미션이 active가 아닙니다.");
			}

			if (dailyContent.daySequence() > activeMission.openedDaySequence()) {
				throw BaseException.from(
					StudyErrorCode.INVALID_STUDY_STATUS,
					"콘텐츠가 아직 열리지 않았습니다. contentDay=" + dailyContent.daySequence()
						+ ", openedDay=" + activeMission.openedDaySequence()
				);
			}

			//  StudyRecord 관련 값은 null/기본값 처리
			return mapStartStudyResultForParent(dailyContent);
		}

		//  자녀면: 기존 로직대로 진행
		// 2) 이미 StudyRecord가 존재하면 그대로 반환
		Optional<StudyRecord> existing = studyRecordPersistencePort.findByMemberIdAndDailyContentId(memberId,
			dailyContentId);
		log.info("기존 StudyRecord 조회 결과: {}", existing);

		if (existing.isPresent()) {
			StudyRecord sr = existing.get();
			log.info("기존 StudyRecord가 존재하므로 재사용: {}", sr);
			return mapStartStudyResult(sr, dailyContent);
		}
		log.info("기존 StudyRecord가 없으므로 새로 생성 진행");
		log.info("memberId: {} , categoryId:{}", memberId, dailyContent.categoryId());
		// 3) Active 미션 조회
		ActiveMissionSnapshot activeMission =
			missionQueryPort.findActiveMission(memberId, dailyContent.categoryId())
				.orElseThrow(() -> BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "활성 미션이 없습니다."));
		log.info("로드된 ActiveMissionSnapshot: {}", activeMission);

		// 4) 미션 Active 상태인지 검증
		if (!activeMission.active()) {
			throw BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "미션이 active가 아닙니다.");
		}
		log.info("미션이 active 상태임을 검증 통과");

		// 5) daySequence 잠금 검증
		// - dailyContent.daySequence <= mission.openedDaySequence : OK
		// - 초과하면 잠금(미래 콘텐츠)
		if (dailyContent.daySequence() > activeMission.openedDaySequence()) {
			throw BaseException.from(
				StudyErrorCode.INVALID_STUDY_STATUS,
				"콘텐츠가 아직 열리지 않았습니다. contentDay=" + dailyContent.daySequence()
					+ ", openedDay=" + activeMission.openedDaySequence()
			);
		}
		log.info("daySequence 잠금 검증 통과: contentDay={}, openedDay={}",
			dailyContent.daySequence(), activeMission.openedDaySequence());

		// 6) StudyRecord 생성(도메인 팩토리)
		StudyRecord created = StudyRecord.openStudy(
			MissionId.of(activeMission.missionId()),
			MemberId.of(memberId),
			CategoryId.of(dailyContent.categoryId()),
			DailyContentId.of(dailyContent.dailyContentId())
		);
		log.info("생성된 StudyRecord: {}", created);

		// 7) 저장
		StudyRecord saved = studyRecordPersistencePort.save(created);
		log.info("저장된 StudyRecord: {}", saved);

		return mapStartStudyResult(saved, dailyContent);
	}

	private StartStudyResult mapStartStudyResultForParent(DailyContentSnapshot dailyContent) {
		return new StartStudyResult(
			null, // studyRecordId 없음
			dailyContent.dailyContentId(),
			dailyContent.categoryId(),
			dailyContent.daySequence(),
			null,  // quizProgressStatus (빠른 처리: null)
			false, // newsUnlocked 기본값
			new StartStudyResult.DailyContentCard(
				dailyContent.title(),
				dailyContent.bodyText(),
				dailyContent.summary(),
				dailyContent.keyword(),
				dailyContent.imageUrl()
			)
		);
	}

	private StartStudyResult mapStartStudyResult(StudyRecord studyRecord, DailyContentSnapshot dailyContent) {
		return new StartStudyResult(
			studyRecord.getId(),
			dailyContent.dailyContentId(),
			dailyContent.categoryId(),
			dailyContent.daySequence(),
			studyRecord.getQuizProgressStatus(),
			studyRecord.isNewsUnlocked(),
			new StartStudyResult.DailyContentCard(
				dailyContent.title(),
				dailyContent.bodyText(),
				dailyContent.summary(),
				dailyContent.keyword(),
				dailyContent.imageUrl()
			)
		);
	}
}

