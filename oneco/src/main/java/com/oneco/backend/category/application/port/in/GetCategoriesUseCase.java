package com.oneco.backend.category.application.port.in;

import java.util.List;

import com.oneco.backend.category.application.dto.CategoriesResult;

public interface GetCategoriesUseCase {
	List<CategoriesResult> getCategories();

}
