package com.oneco.backend.StudyRecord.infrastructure.persistence;

import static com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult.*;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardCategoryReadPort;
import com.oneco.backend.category.infrastructure.CategoryJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HomeDashboardCategoryReadAdapter implements HomeDashboardCategoryReadPort {

	private final CategoryJpaRepository categoryJpaRepository;

	@Override
	public Optional<CategoryResult> findById(Long categoryId) {
		return categoryJpaRepository.findById(categoryId)
			.map(category -> new CategoryResult(
				category.getId(),
				category.getTitle().getValue()
			));
	}
}