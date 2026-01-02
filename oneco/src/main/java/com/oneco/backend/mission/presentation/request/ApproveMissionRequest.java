package com.oneco.backend.mission.presentation.request;

import jakarta.validation.constraints.NotNull;

import com.oneco.backend.mission.application.dto.ApproveMissionCommand;

public record ApproveMissionRequest(
	@NotNull Boolean accept
) {
	public ApproveMissionCommand toCommand(Long missionId, Long recipientId) {
		return new ApproveMissionCommand(missionId, recipientId, accept);
	}
}
