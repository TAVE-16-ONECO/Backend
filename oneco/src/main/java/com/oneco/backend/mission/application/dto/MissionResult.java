package com.oneco.backend.mission.application.dto;

import com.oneco.backend.mission.domain.mission.MissionStatus;

public record MissionResult(
	Long missionId,
	MissionStatus status
) {
}
