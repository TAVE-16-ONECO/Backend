package com.oneco.backend.mission.application.dto;

public record ApproveMissionCommand(
	Long missionId,
	Long recipientId,
	boolean accepted
) {
}
