package com.oneco.backend.category.application.dto;

import com.oneco.backend.category.domain.category.Category;
import com.oneco.backend.category.domain.category.CategoryDifficulty;
import com.oneco.backend.category.domain.category.CategorySummary;
import com.oneco.backend.category.domain.category.CategoryTitle;
import com.oneco.backend.category.domain.category.DisplayOrder;
import com.oneco.backend.category.domain.category.MissionDays;

public record CategoriesResult(
	Long categoryId,
	CategoryTitle categoryTitle,
	CategorySummary summary,
	DisplayOrder displayOrder,
	MissionDays missionDays,
	CategoryDifficulty difficulty
) {
	public static CategoriesResult of(Category category) {
		return new CategoriesResult(
			category.getId(),
			category.getTitle(),
			category.getSummary(),
			category.getDisplayOrder(),
			category.getDefaultMissionDays(),
			category.getDifficulty()
		);
	}
}
