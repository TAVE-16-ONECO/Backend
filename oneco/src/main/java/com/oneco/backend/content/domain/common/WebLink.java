package com.oneco.backend.content.domain.common;

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
			throw new IllegalArgumentException("weblink url은 비어 있을 수 없습니다.");
		}
		String v = url.trim();
		if (!(v.startsWith("http://") || v.startsWith("https://"))) {
			throw new IllegalArgumentException("weblink는 http/https만 허용됩니다.");
		}

		this.url = url.trim();
	}

	public static WebLink of(String url) {
		return new WebLink(url);
	}
}