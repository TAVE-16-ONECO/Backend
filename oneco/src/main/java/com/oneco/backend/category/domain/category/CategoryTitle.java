package com.oneco.backend.category.domain.category;

import com.oneco.backend.category.domain.exception.constant.CategoryErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CategoryTitle {
	private String value;
	public static final int MAX_LENGTH = 50;

	private CategoryTitle(String value) {
		if (value == null || value.isBlank()) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_TITLE_INVALID);
		}
		String v = value.trim();
		if (v.length() > MAX_LENGTH) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_TITLE_INVALID,
				"MAX_LENGTH:" + MAX_LENGTH + " CURRENT_LENGTH:" + v.length());
		}

		this.value = v;
	}

	public static CategoryTitle of(String value) {
		return new CategoryTitle(value);
	}
}
