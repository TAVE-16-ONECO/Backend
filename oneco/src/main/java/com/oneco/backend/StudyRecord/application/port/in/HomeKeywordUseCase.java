package com.oneco.backend.StudyRecord.application.port.in;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeKeywordResult;

public interface HomeKeywordUseCase {
	HomeKeywordResult getKeyword(Long dailyContentId);

}
