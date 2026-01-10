package com.oneco.backend.StudyRecord.application.dto.result;

import java.time.LocalDate;
import java.util.List;

public record HistoryResult(
	List<HistoryItem> historyItems
){
	public record HistoryItem(
		Long DailyContentId,
		String title,
		LocalDate quizAttemptDate,
		String summary,
		boolean isBookmarked,
		List<NewsItemSummary> newsSnapshots
	){}

}
