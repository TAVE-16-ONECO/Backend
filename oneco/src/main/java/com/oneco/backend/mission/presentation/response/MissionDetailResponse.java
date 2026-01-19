package com.oneco.backend.mission.presentation.response;

import java.time.LocalDate;

public record MissionDetailResponse(
	Long missionId,
	String categoryTitle,
	String rewardTitle,
	String rewardMessage,
	LocalDate startDate,
	LocalDate endDate,
	String missionStatus,
	Long memberId,
	Long recipientId,
	Long requesterId,
	String recipientNickname
) {
	public static MissionDetailResponse of(
		Long missionId,
		String categoryTitle,
		String rewardTitle,
		String rewardMessage,
		LocalDate startDate,
		LocalDate endDate,
		String missionStatus,
		Long memberId,
		Long recipientId,
		Long requesterId,
		String recipientNickname
	) {
		return new MissionDetailResponse(missionId, categoryTitle, rewardTitle,rewardMessage, startDate, endDate, missionStatus,
			memberId, recipientId, requesterId, recipientNickname);
	}
}
