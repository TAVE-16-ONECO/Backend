package com.oneco.backend.StudyRecord.application.dto.command;

import jakarta.validation.constraints.NotNull;

/**
 * 마스터하기 버튼 요청 DTO
 * dailyContentId: 학습을 시작할 데일리 콘텐츠 ID
 * memberId는 JWT에서 추출하므로 포함하지 않음
 *
 * @param dailyContentId
 */
public record StartStudyCommand(
	@NotNull Long dailyContentId
) {
}
