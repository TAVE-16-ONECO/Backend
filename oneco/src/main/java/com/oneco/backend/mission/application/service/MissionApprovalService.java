package com.oneco.backend.mission.application.service;

import java.time.LocalDate;

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
		validateNotExpired(mission);

		if (command.accepted()) {
			// 미션 승인 상태일 경우
			mission.acceptApproval();

			// 미션 시작일과 요청 수락일이 같다면 즉시 미션 진행 상태로 변경
			LocalDate startDate = mission.getPeriod().getStartDate();
			LocalDate today = LocalDate.now();

			if (startDate.isBefore(today) || startDate.isEqual(today)) {
				mission.markInProgress(); // 미션을 즉시 진행 상태로 변경
			}

		} else {
			mission.rejectApproval();
		}

		missionPort.save(mission);
		return new MissionResult(mission.getId(), mission.getStatus());
	}

	private void validateRecipient(Mission mission, MemberId recipientId) {
		if (!mission.getRecipientId().equals(recipientId)) {
			throw BaseException.from(MissionErrorCode.MISSION_APPROVAL_FORBIDDEN,
				"미션 수신자가 아닌 사용자는 승인/거절할 수 없습니다.");
		}
	}

	private void validateStatus(Mission mission) {
		if (mission.getStatus() != MissionStatus.APPROVAL_REQUEST) {
			throw BaseException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"승인/거절은 APPROVAL_REQUEST 상태에서만 가능합니다.");
		}
	}

	private void validateNotExpired(Mission mission) {
		LocalDate today = LocalDate.now();
		if (today.isAfter(mission.getPeriod().getEndDate())) {
			throw BaseException.from(MissionErrorCode.LATE_MISSION_APPROVAL,
				"미션 종료일이 지난 후에는 승인할 수 없습니다.");
		}
	}
}
