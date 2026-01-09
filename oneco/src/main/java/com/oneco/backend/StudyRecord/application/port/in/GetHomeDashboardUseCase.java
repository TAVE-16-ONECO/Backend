package com.oneco.backend.StudyRecord.application.port.in;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult;

public interface GetHomeDashboardUseCase {
	HomeDashboardResult getHomeDashboard(Long memberId, Long missionId);
}
