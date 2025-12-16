package com.oneco.backend.content.domain.dailycontent;

import com.oneco.backend.content.domain.exception.constant.ContentErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.Column;
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

	@Column(name = "category_id", nullable = false)
	private Long value;

	private CategoryId(Long value) {
		if (value == null || value <= 0) {
			throw BaseException.from(ContentErrorCode.CATEGORY_ID_INVALID, "입력값=" + value);
		}
		this.value = value;
	}

	public static CategoryId of(Long value) {
		return new CategoryId(value);
	}

}
