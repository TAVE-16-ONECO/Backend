package com.oneco.backend.StudyRecord.application.port.in;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeActiveMissionsResult;

public interface HomeActiveMissionsUseCase {
	HomeActiveMissionsResult getActiveMissions(Long memberId);
}
