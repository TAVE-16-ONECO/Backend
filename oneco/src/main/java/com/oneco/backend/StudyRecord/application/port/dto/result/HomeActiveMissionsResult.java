package com.oneco.backend.StudyRecord.application.port.dto.result;

import java.util.List;

import com.oneco.backend.mission.domain.mission.MissionId;

public record HomeActiveMissionsResult(
	Long missionsCount,
	List<MissionId> activeMissionIds
) {
	public static HomeActiveMissionsResult of(
		Long missionsCount,
		List<MissionId> activeMissionIds
	) {
		return new HomeActiveMissionsResult(
			missionsCount,
			activeMissionIds
		);
	}
}

