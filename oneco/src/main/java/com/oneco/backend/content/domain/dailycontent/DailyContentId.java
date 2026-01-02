package com.oneco.backend.content.domain.dailycontent;

import com.oneco.backend.content.domain.exception.constant.ContentErrorCode;
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
public class DailyContentId {

	private Long value;

	private DailyContentId(Long value) {
		if (value == null || value <= 0) {
			throw BaseException.from(ContentErrorCode.INVALID_DAILYCONTENT_ID, "입력값=" + value);
		}
		this.value = value;
	}

	public static DailyContentId of(Long value) {
		return new DailyContentId(value);
	}

}
