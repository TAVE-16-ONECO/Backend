package com.oneco.backend.content.domain.common;

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
public class WebLink {

	@Column(nullable = false, length = 500)
	private String url;

	private WebLink(String url) {
		if (url == null || url.isBlank()) {
			throw BaseException.from(ContentErrorCode.WEBLINK_URL_EMPTY);
		}
		String v = url.trim();
		if (!(v.startsWith("http://") || v.startsWith("https://"))) {
			throw BaseException.from(ContentErrorCode.WEBLINK_SCHEME_INVALID, "입력값=" + v);
		}

		this.url = url.trim();
	}

	public static WebLink of(String url) {
		return new WebLink(url);
	}
}