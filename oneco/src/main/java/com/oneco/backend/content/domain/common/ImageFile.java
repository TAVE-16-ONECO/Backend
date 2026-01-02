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
public class ImageFile {

	@Column(nullable = false)
	private String url;

	private ImageFile(String url) {
		if (url == null || url.isBlank()) {
			throw BaseException.from(ContentErrorCode.IMAGE_URL_EMPTY);
		}
		this.url = url.trim();
	}

	public static ImageFile of(String url) {
		return new ImageFile(url);
	}
}
