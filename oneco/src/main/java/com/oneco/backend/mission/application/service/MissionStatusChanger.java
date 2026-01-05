package com.oneco.backend.mission.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.judge.MissionJudgementService;
import com.oneco.backend.mission.domain.judge.dto.MissionFailureJudgement;
import com.oneco.backend.mission.domain.judge.dto.MissionProgressSnapshot;
import com.oneco.backend.mission.domain.judge.dto.MissionSuccessJudgement;
import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.domain.mission.MissionId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MissionStatusChanger {

	private final MissionJudgementService missionJudgementService;
	private final MissionPersistencePort missionPort;

	// 미션 진행중으로 상태 변경한다.
	// 미션 Period 에서 시작일이 되면 호출된다.
	// todo: 스케줄러에서 미션 시작일이 되면 호출하도록 구현 필요함.
	// processBatchFailure
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void toInProgress(Mission mission) {
		mission.markInProgress();
		missionPort.save(mission); // 명시적으로 영속성 계층에 변경 사항을 저장

	}

	// 미션 성공으로 상태를 변경한다.
	// snapshot: 미션 진행 상황 스냅샷에 맞춰서 전달한다.(중요)
	// 미션 도메인의 toCompleted() 메서드는 스냅샷 정보를 받아서 성공 여부를 판단하고 완료로 변경한다.
	public MissionSuccessJudgement toCompleted(MissionId missionId, MissionProgressSnapshot snapshot) {
		// 미션 성공 여부 판단
		MissionSuccessJudgement judgement = missionJudgementService.judgeSuccess(snapshot);

		// 성공하지 못한 경우 예외 발생
		if (!judgement.success()) {
			throw BaseException.from(
				MissionErrorCode.INVALID_MISSION_JUDGE,
				judgement.reason()
			);
		}

		// MissionId로 미션을 조회하고 완료 상태로 변경
		Mission mission = missionPort.findById(missionId.getValue());
		mission.markCompleted();

		// 도메인 요구사항 중, 미션 성공 시, 자동 보상 요청 상태로 변경한다.
		toRequestReward(mission);

		return judgement;
	}

	// 미션 보상 요청 상태로 변경
	// 미션이 완료되면 자동으로 호출해서 보상 요청 상태로 변경한다.
	public void toRequestReward(Mission mission) {
		mission.requestReward();
	}

	// 미션 보상 승인 상태로 변경
	// 사용자가 보상을 승인 요청을 하면 호출된다.
	public void toApproveReward(MissionId missionId) {
		// MissionId로 미션을 조회하고 보상 승인 상태로 변경
		Mission mission = missionPort.findById(missionId.getValue());
		mission.completeReward();
	}

	// =============================
	// 미션 실패(조기 실패 / 배치 처리) 관련 메서드
	// =============================

	// 조기 실패 판정 처리 메서드
	// 사용자가 퀴즈를 풀다가 다 맞추지 못했고, 정답률이 성공률에 미치지 못할 때 상태를 변경한다.
	// snapshot: 미션 진행 상황 스냅샷에 맞춰서 전달한다.(중요)
	// 미션 도메인의 toFailed() 메서드는 스냅샷 정보를 받아서 성공 가능성이 있는지 판단하고 실패로 변경한다.
	public MissionFailureJudgement toFailed(MissionId missionId, MissionProgressSnapshot snapshot) {
		// 미션 실패 여부 판단
		MissionFailureJudgement judgement = missionJudgementService.judgeFailure(snapshot);

		// 실패하지 않은 경우 예외 발생
		if (!judgement.failed()) {
			throw BaseException.from(
				MissionErrorCode.INVALID_MISSION_JUDGE,
				judgement.reason()
			);
		}

		// MissionId로 미션을 조회하고 실패 상태로 변경
		Mission mission = missionPort.findById(missionId.getValue());
		mission.markFailed();
		return judgement;
	}

	// MissionBatchProcessor 에서 호출하는 메서드
	// 미션 마감 기한이 지났는지는 미션 도메인의 스케쥴러에서 처리하므로 protected로 감싼다.
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void processBatchFailure(Mission mission) {
		mission.markFailed(); // 1. 도메인 상태 변경

		// 2. 명시적으로 영속성 계층에 변경 사항을 저장
		// Dirty Checking(JPA)이 동작하겠지만 JPA 구현체에 따라 다를 수 있으므로 명시적으로 저장
		missionPort.save(mission);
	}
}
