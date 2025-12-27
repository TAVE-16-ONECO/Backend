package com.oneco.backend.mission.presentation.response;

public record MissionResponse(
	Long missionId,
	String missionTitle,
	String rewardTitle,
	String missionStatus
) {
}
