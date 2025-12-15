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
public class ImageFile {

	@Column(nullable = false)
	private String url;

	private ImageFile(String url) {
		if (url == null || url.isBlank()) {
			throw new IllegalArgumentException("이미지 URL은 비어있을 수 없습니다.");
		}
		this.url = url.trim();
	}

	public static ImageFile of(String url) {
		return new ImageFile(url);
	}
}
