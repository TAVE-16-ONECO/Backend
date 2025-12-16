package com.oneco.backend.content.domain.quiz;

import com.oneco.backend.content.domain.exception.constant.ContentErrorCode;
import com.oneco.backend.global.exception.BaseException;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DB에는 JSON 문자열로 저장
 * 도메인 코드에서는 List 구조 의미 있는 타입으로 사용
 */
@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizOption {

	// 보기 텍스트
	// 실제 DB에는 QuizOptions 객체의 JSON 문자열로 저장
	private String text;

	private QuizOption(String text) {
		if (text == null || text.isBlank()) {
			throw BaseException.from(ContentErrorCode.QUIZ_OPTION_TEXT_EMPTY);
		}
		this.text = text.trim();
	}

	public static QuizOption of(String text) {
		return new QuizOption(text);
	}
}
