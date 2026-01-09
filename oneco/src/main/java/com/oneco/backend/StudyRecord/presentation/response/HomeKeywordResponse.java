package com.oneco.backend.StudyRecord.presentation.response;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeKeywordResult;

public record HomeKeywordResponse(
	Long dailyContentId,
	String keyword
) {
	public static HomeKeywordResponse from(HomeKeywordResult result) {
		return new HomeKeywordResponse(
			result.dailyContentId(),
			result.keyword().getValue()
		);
	}
}
