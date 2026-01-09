package com.oneco.backend.category.application.port.out;

import java.util.List;

import com.oneco.backend.category.domain.category.Category;

public interface CategoryPersistencePort {
	List<Category> findAllCategories();

}
