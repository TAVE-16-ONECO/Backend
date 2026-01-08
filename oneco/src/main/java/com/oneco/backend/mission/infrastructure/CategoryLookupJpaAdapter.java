package com.oneco.backend.mission.infrastructure;

import org.springframework.stereotype.Component;

import com.oneco.backend.category.domain.category.Category;
import com.oneco.backend.category.domain.category.CategoryId;
import com.oneco.backend.category.domain.category.MissionDays;
import com.oneco.backend.category.domain.exception.constant.CategoryErrorCode;
import com.oneco.backend.category.infrastructure.CategoryJpaRepository;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.application.port.out.CategoryLookupPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryLookupJpaAdapter implements CategoryLookupPort {

	private final CategoryJpaRepository categoryJpaRepository;

	@Override
	public MissionDays getDefaultMissionDays(CategoryId categoryId) {
		return categoryJpaRepository.findById(categoryId.getValue())
			.map(Category::getDefaultMissionDays)
			.orElseThrow(() -> BaseException.from(
				CategoryErrorCode.INVALID_CATEGORY_ID,
				"Invalid categoryId: " + categoryId.getValue()
			));
	}
}
