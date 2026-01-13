package com.oneco.backend.StudyRecord.application.dto.result;

import java.time.LocalDate;
import java.util.List;

import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSummary;

public record HistoryResult(
	// 아이템 더 있는지
	boolean hasNext,
	Long nextId,
	LocalDate nextSubmittedDate,


	// 부모인 경우 자녀 멤버들 정보
	List<MemberItem> memberItems,
	// 히스토리 아이템들
	List<HistoryItem> historyItems
	){
}
