package com.oneco.backend.dailycontent.domain.dailycontent;

import com.oneco.backend.dailycontent.domain.exception.constant.ContentErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Keyword {
	public static final int MAX_LENGTH = 100;

	@Column(name = "keyword", nullable = false, length = 100)
	private String value;

	private Keyword(String value) {
		if (value == null || value.isBlank()) {
			throw BaseException.from(ContentErrorCode.KEYWORD_EMPTY);
		}
		String v = value.trim();
		if (v.length() > MAX_LENGTH) {
			throw BaseException.from(ContentErrorCode.KEYWORD_TOO_LONG,
				"maxLength:" + MAX_LENGTH + ", actualLength:" + v.length());
		}
		this.value = v;
	}

	public static Keyword of(String value) {
		return new Keyword(value);
	}
}
