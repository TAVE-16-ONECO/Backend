package com.oneco.backend.category.domain.exception.constant;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

	CATEGORY_SUMMARY_INVALID(HttpStatus.BAD_REQUEST, "카테고리 요약은 비어 있을 수 없습니다.",
		"CATEGORY_400_001"),

	CATEGORY_TITLE_INVALID(HttpStatus.BAD_REQUEST, "카테고리 제목은 비어 있을 수 없습니다.",
		"CATEGORY_400_002"),

	CATEGORY_MISSION_DAYS_INVALID(HttpStatus.BAD_REQUEST, "카테고리 미션은 1이상이어야 합니다.",
		"CATEGORY_400_003"),

	CATEGORY_DISPLAY_ORDER_INVALID(HttpStatus.BAD_REQUEST, "카테고리 순서는 1이상이어야 합니다.",
		"CATEGORY_400_004"),

	CATEGORY_REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "카테고리 필수 값이 누락되었습니다.",
		"CATEGORY_400_005"),

	CATEGORY_ALREADY_HIDDEN(HttpStatus.BAD_REQUEST, "카테고리가 이미 숨겨져 있습니다.",
		"CATEGORY_400_006"),

	CATEGORY_ALREADY_VISIBLE(HttpStatus.BAD_REQUEST, "카테고리가 이미 보이고 있습니다.",
		"CATEGORY_400_007");

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
