
package com.oneco.backend.content.domain.exception.constant;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentErrorCode implements ErrorCode {

	// ===== Common / Validation =====
	INVALID_SEQUENCE_VALUE(HttpStatus.BAD_REQUEST, "순번 값이 올바르지 않습니다.", "CONTENT_400_001"),
	REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "필수 값이 누락되었습니다.", "CONTENT_400_002"),
	INVALID_VALUE(HttpStatus.BAD_REQUEST, "값이 올바르지 않습니다.", "CONTENT_400_003"),

	// ===== DailyContent =====
	DAILY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DailyContent를 찾을 수 없습니다.", "CONTENT_404_001"),
	NEWS_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 순번의 뉴스가 존재하지 않습니다.", "CONTENT_404_002"),
	QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 순번의 퀴즈가 존재하지 않습니다.", "CONTENT_404_003"),
	NEWS_ORDER_DUPLICATE(HttpStatus.CONFLICT, "동일한 뉴스 순번이 이미 존재합니다.", "CONTENT_409_001"),
	QUIZ_ORDER_DUPLICATE(HttpStatus.CONFLICT, "동일한 퀴즈 순번이 이미 존재합니다.", "CONTENT_409_002"),

	// ===== Value Objects =====
	IMAGE_URL_EMPTY(HttpStatus.BAD_REQUEST, "이미지 URL은 비어 있을 수 없습니다.", "CONTENT_400_101"),
	WEBLINK_URL_EMPTY(HttpStatus.BAD_REQUEST, "링크 URL은 비어 있을 수 없습니다.", "CONTENT_400_102"),
	WEBLINK_SCHEME_INVALID(HttpStatus.BAD_REQUEST, "링크는 http/https만 허용됩니다.", "CONTENT_400_103"),
	CATEGORY_ID_INVALID(HttpStatus.BAD_REQUEST, "categoryId는 양수여야 합니다.", "CONTENT_400_104"),
	KEYWORD_EMPTY(HttpStatus.BAD_REQUEST, "키워드는 비어 있을 수 없습니다.", "CONTENT_400_105"),
	KEYWORD_TOO_LONG(HttpStatus.BAD_REQUEST, "키워드 길이가 너무 깁니다.", "CONTENT_400_106"),
	TITLE_EMPTY(HttpStatus.BAD_REQUEST, "title은 비어 있을 수 없습니다.", "CONTENT_400_107"),
	SUMMARY_EMPTY(HttpStatus.BAD_REQUEST, "summary는 비어 있을 수 없습니다.", "CONTENT_400_108"),
	BODY_EMPTY(HttpStatus.BAD_REQUEST, "body는 비어 있을 수 없습니다.", "CONTENT_400_109"),
	TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "title 길이가 너무 깁니다.", "CONTENT_400_110"),
	SUMMARY_TOO_LONG(HttpStatus.BAD_REQUEST, "summary 길이가 너무 깁니다.", "CONTENT_400_111"),

	// ===== NewsItem =====
	NEWS_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "뉴스 title은 비어 있을 수 없습니다.", "CONTENT_400_301"),
	NEWS_WEBLINK_REQUIRED(HttpStatus.BAD_REQUEST, "뉴스 webLink는 null일 수 없습니다.", "CONTENT_400_302"),
	NEWS_ITEM_ORDER_REQUIRED(HttpStatus.BAD_REQUEST, "뉴스 newsItemOrder는 null일 수 없습니다.", "CONTENT_400_303"),
	NEWS_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "뉴스 imageFile는 null일 수 없습니다.", "CONTENT_400_304"),

	// ===== Quiz =====
	QUIZ_QUESTION_EMPTY(HttpStatus.BAD_REQUEST, "퀴즈 질문은 비어 있을 수 없습니다.", "CONTENT_400_201"),
	ANSWER_INDEX_INVALID(HttpStatus.BAD_REQUEST, "정답 인덱스는 1 이상이어야 합니다.", "CONTENT_400_202"),
	ANSWER_INDEX_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "정답 인덱스가 보기 범위를 벗어났습니다.", "CONTENT_400_203"),
	QUIZ_OPTION_TEXT_EMPTY(HttpStatus.BAD_REQUEST, "퀴즈 보기 텍스트는 비어 있을 수 없습니다.", "CONTENT_400_204"),
	QUIZ_OPTIONS_EMPTY(HttpStatus.BAD_REQUEST, "퀴즈 보기는 비어 있을 수 없습니다.", "CONTENT_400_205"),
	QUIZ_OPTIONS_NULL_ELEMENT(HttpStatus.BAD_REQUEST, "퀴즈 보기에 null이 포함될 수 없습니다.", "CONTENT_400_206"),
	QUIZ_OPTIONS_COUNT_INVALID(HttpStatus.BAD_REQUEST, "퀴즈 보기 개수가 올바르지 않습니다.", "CONTENT_400_207"),
	QUIZ_OPTIONS_DUPLICATE_TEXT(HttpStatus.BAD_REQUEST, "퀴즈 보기 텍스트는 중복될 수 없습니다.", "CONTENT_400_208"),

	// ===== Persistence / Converter =====
	QUIZ_OPTIONS_JSON_SERIALIZE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "퀴즈 보기를 JSON으로 변환하는 데 실패했습니다.",
		"CONTENT_500_001"),
	QUIZ_OPTIONS_JSON_DESERIALIZE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "JSON을 퀴즈 보기로 변환하는 데 실패했습니다.",
		"CONTENT_500_002");

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;

}