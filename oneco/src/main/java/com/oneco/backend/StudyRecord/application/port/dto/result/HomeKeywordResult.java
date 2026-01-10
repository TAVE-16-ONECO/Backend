package com.oneco.backend.StudyRecord.application.port.dto.result;

import com.oneco.backend.dailycontent.domain.dailycontent.Keyword;

public record HomeKeywordResult(
	Long dailyContentId,
	Keyword keyword
) {
	public static HomeKeywordResult of(Long dailyContentId, Keyword keyword) {
		return new HomeKeywordResult(dailyContentId, keyword);
	}
}
