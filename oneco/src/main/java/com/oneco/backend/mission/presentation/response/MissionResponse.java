package com.oneco.backend.mission.presentation.response;

public record MissionResponse(
	Long missionId,
	String categoryTitle,
	String rewardTitle,
	String missionStatus
) {
}
