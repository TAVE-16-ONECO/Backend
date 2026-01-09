package com.oneco.backend.category.presentation.response;

import java.util.List;

import com.oneco.backend.category.application.dto.CategoriesResult;

public record CategoriesResponse(
	List<CategoryResult> categories
) {
	public static CategoriesResponse from(List<CategoriesResult> categoryResults) {
		List<CategoryResult> categories = categoryResults.stream()
			.map(CategoryResult::from)
			.toList();
		return new CategoriesResponse(categories);
	}

	public record CategoryResult(
		Long categoryId,
		String categoryTitle,
		String summary,
		Integer displayOrder,
		int missionDays,
		String difficulty
	) {
		public static CategoryResult from(CategoriesResult categoryResult) {
			return new CategoryResult(
				categoryResult.categoryId(),
				categoryResult.categoryTitle().getValue(),
				categoryResult.summary().getValue(),
				categoryResult.displayOrder() != null ? categoryResult.displayOrder().getValue() : null,
				categoryResult.missionDays().getValue(),
				categoryResult.difficulty().name()
			);
		}
	}
}
