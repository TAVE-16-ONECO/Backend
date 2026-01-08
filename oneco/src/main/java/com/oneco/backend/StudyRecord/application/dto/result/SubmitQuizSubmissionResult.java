package com.oneco.backend.StudyRecord.application.dto.result;

import java.util.List;

import com.oneco.backend.StudyRecord.domain.quizAttempt.AttemptResult;
import com.oneco.backend.StudyRecord.domain.studyRecord.QuizProgressStatus;

/**
 * {
 * "studyRecordId": 1201,
 * "dailyContentId": 345,
 * "attempt": {
 * "attemptId": 9001,
 * "attemptNo": 1,
 * "status": "COMPLETED"
 * },
 * "grading": {
 * "correctCount": 2,
 * "totalCount": 3,
 * "result": "FAIL"
 * },
 * "quizProgressStatus": "RETRY_AVAILABLE",
 * "newsUnlocked": false,
 * "remainingAttempts": 1
 * }
 */
public record SubmitQuizSubmissionResult(
	Long studyRecordId,
	Long dailyContentId,

	AttemptSummary attemptSummary,
	GradingSummary grading,
	List<NewsItemSummary> newsItems,
	QuizProgressStatus quizProgressStatus,
	boolean newsUnlocked,
	int remainingAttempts
) {
	public record NewsItemSummary(
		String title,
		String url,
		String imageUrl
	) {
	}

	public record GradingSummary(
		int correctCount,
		int totalCount,
		AttemptResult result
	) {
	}
}
