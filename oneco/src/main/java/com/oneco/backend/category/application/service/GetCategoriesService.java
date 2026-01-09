package com.oneco.backend.category.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.category.application.dto.CategoriesResult;
import com.oneco.backend.category.application.port.in.GetCategoriesUseCase;
import com.oneco.backend.category.application.port.out.CategoryPersistencePort;
import com.oneco.backend.category.domain.category.Category;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCategoriesService implements GetCategoriesUseCase {

	private final CategoryPersistencePort categoryPersistencePort;

	@Override
	public List<CategoriesResult> getCategories() {
		List<Category> categories = categoryPersistencePort.findAllCategories();

		return categories.stream()
			.map(CategoriesResult::of)
			.toList();
	}
}
