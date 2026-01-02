package com.oneco.backend.StudyRecord.application.dto.result;

import com.oneco.backend.StudyRecord.domain.quizAttempt.AttemptStatus;

public record AttemptSummary(
	Long attemptId,
	int attemptNo,
	AttemptStatus status
) {
}