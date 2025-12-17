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
public class MissionDays {
	private int value;
	public static final int MAX_DAYS = 365;

	private MissionDays(int value) {
		if (value <= 0) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_MISSION_DAYS_INVALID);
		}
		if (value > MAX_DAYS) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_MISSION_DAYS_INVALID,
				"MAX_DAYS:" + MAX_DAYS + " CURRENT_DAYS:" + value);
		}
		this.value = value;
	}

	public static MissionDays of(int value) {
		return new MissionDays(value);
	}
}
