package com.oneco.backend.mission.application.dto;

import java.time.LocalDate;

// 미션을 생성하는데 필요한 요청 값
public record CreateMissionCommand(
	Long requesterId,
	Long recipientId,
	Long categoryId,
	LocalDate startDate,
	LocalDate endDate,
	String title,
	String message
) {
}
