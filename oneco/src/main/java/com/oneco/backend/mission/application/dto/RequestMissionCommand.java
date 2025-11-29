package com.oneco.backend.mission.application.dto;

import java.time.LocalDate;

// 미션을 생성하는데 필요한 요청 값
public record RequestMissionCommand(
	Long memberId,
	Long familyRelationId,
	LocalDate startDate,
	LocalDate endDate,
	String title,
	String message
) {
}
