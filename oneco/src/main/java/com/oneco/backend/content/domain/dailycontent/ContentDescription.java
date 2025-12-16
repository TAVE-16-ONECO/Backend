package com.oneco.backend.content.domain.dailycontent;

import com.oneco.backend.content.domain.exception.constant.ContentErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 값 객체는 식별자가 아닌 속성 값 자체가 같으면 같은 객체로 취급해야 한다.
 *
 * @EqualsAndHashCode -> 모든 필드의 값이 같으면 같은 객체로 판단해줌
 * 엔티티는 만들 때 쓰면 안됨
 * -> 엔티티는 필드 값이 바뀌어도 ID가 같으면 같은 객체여야 하기 때문이다.
 */
@EqualsAndHashCode
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentDescription {

	public static final int TITLE_MAX_LENGTH = 200;
	public static final int SUMMARY_MAX_LENGTH = 300;

	// ddl-auto: none일 때 상수값을 변해도 테이블에 반영이 안되므로 주의
	// 상수 값을 바꿨다면, 반드시 테이블을 수정하는 명령을 직접 실행해야 한다.
	// 예: ALTER TABLE daily_content MODIFY COLUMN title VARCHAR(300);
	@Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
	private String title;

	@Column(name = "summary", nullable = false, length = SUMMARY_MAX_LENGTH)
	private String summary;

	@Column(name = "body_text", nullable = false, columnDefinition = "TEXT")
	private String bodyText;

	private ContentDescription(String title, String summary, String body) {
		this.title = require(title, "title");
		this.summary = require(summary, "summary");
		this.bodyText = require(body, "body");
		if (this.title.length() > TITLE_MAX_LENGTH) {
			throw BaseException.from(ContentErrorCode.TITLE_TOO_LONG,"TITLE_MAX_LENGTH:"+TITLE_MAX_LENGTH+ "CURRENT_LENGTH:" + this.title.length());
		}
		if (this.summary.length() > SUMMARY_MAX_LENGTH) {
			throw BaseException.from(ContentErrorCode.SUMMARY_TOO_LONG,"SUMMARY_MAX_LENGTH:"+SUMMARY_MAX_LENGTH+ "CURRENT_LENGTH:" + this.summary.length());
		}
	}

	public static ContentDescription of(String title, String summary, String body) {
		return new ContentDescription(title, summary, body);
	}

	private String require(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			switch (fieldName) {
				case "title" -> throw BaseException.from(ContentErrorCode.TITLE_EMPTY);
				case "summary" -> throw BaseException.from(ContentErrorCode.SUMMARY_EMPTY);
				case "body" -> throw BaseException.from(ContentErrorCode.BODY_EMPTY);
				default -> throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, fieldName);
			}
		}
		return value.trim();
	}

	public ContentDescription withSummary(String summary) {
		return new ContentDescription(this.title, summary, this.bodyText);
	}

	public ContentDescription withTitle(String title) {
		return new ContentDescription(title, this.summary, this.bodyText);

	}

	public ContentDescription withBodyText(String bodyText) {
		return new ContentDescription(this.title, this.summary, bodyText);
	}
}