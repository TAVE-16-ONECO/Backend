package com.oneco.backend.StudyRecord.presentation.response;

import java.util.List;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeActiveMissionsResult;
import com.oneco.backend.mission.domain.mission.MissionId;

public record HomeActiveMissionsResponse(
	// 진행중인 미션 수
	Long missionCount,
	// 진행중인 미션 ID 리스트
	List<Long> activeMissionIds
) {
	public static HomeActiveMissionsResponse from(HomeActiveMissionsResult result) {
		return new HomeActiveMissionsResponse(
			result.missionsCount(),
			result.activeMissionIds().stream()
				.map(MissionId::getValue)
				.toList()
		);
	}
}
