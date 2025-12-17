package com.oneco.backend.Category.domain.Category;

import com.oneco.backend.Category.domain.exception.constant.CategoryErrorCode;
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
public class DisplayOrder {
	private int value;

	private DisplayOrder(int value) {
		if (value <= 0) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_DISPLAY_ORDER_INVALID);
		}
		this.value = value;
	}

	public static DisplayOrder of(int value) {
		return new DisplayOrder(value);
	}
}
