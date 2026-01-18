package com.oneco.backend.mission.presentation.response;

import java.time.LocalDate;

public record MissionDetailResponse(
	Long missionId,
	String categoryTitle,
	String rewardTitle,
	LocalDate startDate,
	LocalDate endDate,
	String missionStatus,
	Long memberId,
	Long recipientId,
	Long requesterId,
	String nickname
) {
	public static MissionDetailResponse of(
		Long missionId,
		String categoryTitle,
		String rewardTitle,
		LocalDate startDate,
		LocalDate endDate,
		String missionStatus,
		Long memberId,
		Long recipientId,
		Long requesterId,
		String nickname
	) {
		return new MissionDetailResponse(missionId, categoryTitle, rewardTitle, startDate, endDate, missionStatus,
			memberId, recipientId, requesterId, nickname);
	}
}
