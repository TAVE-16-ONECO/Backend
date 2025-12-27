package com.oneco.backend.category.domain.category;

import com.oneco.backend.category.domain.exception.constant.CategoryErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryId {

	private Long value;

	private CategoryId(Long value) {
		if (value == null || value <= 0) {
			throw BaseException.from(CategoryErrorCode.INVALID_CATEGORY_ID, "입력값=" + value);
		}
		this.value = value;
	}

	public static CategoryId of(Long value) {
		return new CategoryId(value);
	}

}


