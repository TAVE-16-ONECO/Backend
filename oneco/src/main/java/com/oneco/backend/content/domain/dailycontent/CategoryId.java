package com.oneco.backend.content.domain.dailycontent;

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
			throw new IllegalArgumentException("categoryId는 양수여야 합니다.");
		}
		this.value = value;
	}

	public static CategoryId of(Long value){
		return new CategoryId(value);
	}

}
