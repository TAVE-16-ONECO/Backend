package com.oneco.backend.mission.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.mission.application.dto.ApproveMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;
import com.oneco.backend.mission.application.port.in.ApproveMissionUseCase;
import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.domain.mission.MissionStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MissionApprovalService implements ApproveMissionUseCase {

	private final MissionPersistencePort missionPort;

	@Override
	@Transactional
	public MissionResult decide(ApproveMissionCommand command) {
		// 미션 조회
		Mission mission = missionPort.findById(command.missionId());

		// 수신자 검증
		MemberId recipientId = MemberId.of(command.recipientId());
		validateRecipient(mission, recipientId);
		validateStatus(mission);

		if (command.accepted()) {
			mission.acceptApproval();
		} else {
			mission.rejectApproval();
		}

		missionPort.save(mission);
		return new MissionResult(mission.getId(), mission.getStatus());
	}

	private void validateRecipient(Mission mission, MemberId recipientId) {
		if (!mission.getRecipientId().equals(recipientId)) {
			throw BaseException.from(MissionErrorCode.MISSION_APPROVAL_FORBIDDEN);
		}
	}

	private void validateStatus(Mission mission) {
		if (mission.getStatus() != MissionStatus.APPROVAL_REQUEST) {
			throw BaseException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"승인/거절은 APPROVAL_REQUEST 상태에서만 가능합니다.");
		}
	}
}
