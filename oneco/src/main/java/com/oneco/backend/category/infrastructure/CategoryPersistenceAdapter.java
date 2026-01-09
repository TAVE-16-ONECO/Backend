package com.oneco.backend.category.infrastructure;

import java.util.List;

import org.springframework.stereotype.Component;

import com.oneco.backend.category.application.port.out.CategoryPersistencePort;
import com.oneco.backend.category.domain.category.Category;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryPersistencePort {
	private final CategoryJpaRepository repository;

	@Override
	public List<Category> findAllCategories() {
		return repository.findAll();
	}
}
