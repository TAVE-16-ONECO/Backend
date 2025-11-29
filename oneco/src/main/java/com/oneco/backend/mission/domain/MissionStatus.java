package com.oneco.backend.mission.domain;

import lombok.Getter;

@Getter
public enum MissionStatus {

	APPROVAL_REQUEST(MissionPhase.IN_PROGRESS), // 승인 요청
	APPROVAL_ACCEPTED(MissionPhase.IN_PROGRESS), // 승인 수락
	IN_PROGRESS(MissionPhase.IN_PROGRESS), // 진행 중

	APPROVAL_REJECTED(MissionPhase.FINISHED), // 승인 거절
	COMPLETED(MissionPhase.FINISHED), // 완료
	FAILED(MissionPhase.FINISHED), // 실패
	REWARD_REQUESTED(MissionPhase.FINISHED), // 보상 요청
	REWARD_COMPLETED(MissionPhase.FINISHED); // 보상 완료

	private final MissionPhase phase;

	MissionStatus(MissionPhase phase) {
		this.phase = phase;
	}

	public boolean isInProgress() {
		return this.phase == MissionPhase.IN_PROGRESS;
	}

	public boolean isFinished() {
		return this.phase == MissionPhase.FINISHED;
	}
}
