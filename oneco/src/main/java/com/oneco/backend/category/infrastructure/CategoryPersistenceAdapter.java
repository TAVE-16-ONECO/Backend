package com.oneco.backend.category.infrastructure;

import java.util.List;

import org.springframework.stereotype.Component;

import org.springframework.data.domain.Sort;

import com.oneco.backend.category.application.port.out.CategoryPersistencePort;
import com.oneco.backend.category.domain.category.Category;
import com.oneco.backend.category.domain.category.CategoryVisibility;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryPersistencePort {
	private final CategoryJpaRepository repository;

	@Override
	public List<Category> findAllCategories() {
		Sort sort = Sort.by(
			Sort.Order.asc("displayOrder.value"),
			Sort.Order.asc("id")
		);
		return repository.findByVisibility(CategoryVisibility.VISIBLE, sort);
	}
}
