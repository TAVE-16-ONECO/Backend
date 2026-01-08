package com.oneco.backend.StudyRecord.application.port.out;

import static com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult.*;

import java.util.Optional;

public interface HomeDashboardCategoryReadPort {
	Optional<CategoryResult> findById(Long categoryId);
}