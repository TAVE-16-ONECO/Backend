package com.oneco.backend.Category.domain.Category;

import com.oneco.backend.global.exception.BaseException;

import com.oneco.backend.Category.domain.exception.constant.CategoryErrorCode;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CategorySummary {
	String value;
	public static final int MAX_LENGTH = 100;

	private CategorySummary(String value) {
		if (value == null || value.isBlank()) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_SUMMARY_INVALID);
		}
		if (value.length() > MAX_LENGTH) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_SUMMARY_INVALID,
				"MAX_LENGTH:" + MAX_LENGTH + "CURRENT_LENGTH:" + value.length());
		}
		this.value = value;
	}

	public static CategorySummary of(String value) {
		return new CategorySummary(value);
	}
}
