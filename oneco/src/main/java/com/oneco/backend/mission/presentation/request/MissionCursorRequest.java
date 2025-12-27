package com.oneco.backend.mission.presentation.request;

import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionCursorRequest(
	@Schema(description = "이전 페이지의 마지막 미션 ID (첫 요청 시 생략)", example = "5", nullable = true)
	@Positive Long lastId,
	@Schema(description = "페이지당 조회 개수 (기본 5)", example = "5")
	Integer size
) {
	public MissionCursorRequest {
		if (size == null) {
			size = 5;
		}
	}
}
