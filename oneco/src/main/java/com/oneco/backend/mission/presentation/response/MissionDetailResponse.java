package com.oneco.backend.mission.presentation.response;

import java.time.LocalDate;

public record MissionDetailResponse(
	Long missionId,
	String categoryTitle,
	String rewardTitle,
	LocalDate startDate,
	LocalDate endDate,
	String missionStatus
) {
	public static MissionDetailResponse of(
		Long missionId,
		String categoryTitle,
		String rewardTitle,
		LocalDate startDate,
		LocalDate endDate,
		String missionStatus
	) {
		return new MissionDetailResponse(missionId, categoryTitle, rewardTitle, startDate, endDate, missionStatus);
	}
}
