package com.oneco.backend.StudyRecord.application.dto.command;

import jakarta.validation.constraints.NotNull;

/**
 * 퀴즈 시도 시작 요청 DTO
 * studyRecordId: 퀴즈 시도를 시작할 학습 기록 ID
 *
 * @param studyRecordId
 */
public record StartQuizAttemptCommand(
	@NotNull Long studyRecordId
) {
	public static StartQuizAttemptCommand with(Long studyRecordId) {
		return new StartQuizAttemptCommand(studyRecordId);
	}
}
