package com.oneco.backend.StudyRecord.application.dto.result;

import java.util.List;

import com.oneco.backend.StudyRecord.domain.studyRecord.QuizProgressStatus;

public record StartQuizAttemptResult(
	Long studyRecordId,
	Long dailyContentId,
	AttemptSummary attempt,
	QuizProgressStatus quizProgressStatus,
	int remainingAttempts,
	List<QuizView> quizzes
) {
}
