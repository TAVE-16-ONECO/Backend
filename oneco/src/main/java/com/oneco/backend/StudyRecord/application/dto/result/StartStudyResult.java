package com.oneco.backend.StudyRecord.application.dto.result;

import com.oneco.backend.StudyRecord.domain.studyRecord.QuizProgressStatus;

public record StartStudyResult(
	Long studyRecordId,
	Long dailyContentId,
	Long categoryId,
	int daySequence,
	QuizProgressStatus quizProgressStatus,

	boolean newsUnlocked,
	DailyContentCard dailyContent
) {
	// 데일리 콘텐츠 카드 정보
	// 먼저 생성 후 StartStudyResult에 포함
	public record DailyContentCard(
		String title,
		String bodyText,
		String summary,
		String keyword,
		String imageUrl
	) {
	}
}
