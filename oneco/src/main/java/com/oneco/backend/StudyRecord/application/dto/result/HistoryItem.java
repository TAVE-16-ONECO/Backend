package com.oneco.backend.StudyRecord.application.dto.result;

import java.time.LocalDate;
import java.util.List;

import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSummary;

import lombok.Builder;

@Builder
public record HistoryItem(
	Long studyRecordId,
	LocalDate quizAttemptDate,
	boolean isBookmarked,
	DailyContentSummary dailyContentSummary
) {
}
