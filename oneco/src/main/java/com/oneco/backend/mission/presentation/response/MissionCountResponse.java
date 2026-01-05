package com.oneco.backend.mission.presentation.response;

public record MissionCountResponse(
	long totalMissionCount,
	long inProgressMissionCount,
	long finishedMissionCount
) {
	public static MissionCountResponse of(long totalMissionCount, long inProgressMissionCount,
		long finishedMissionCount) {
		return new MissionCountResponse(totalMissionCount, inProgressMissionCount, finishedMissionCount);
	}
}
