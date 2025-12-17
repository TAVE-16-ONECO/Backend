package com.oneco.backend.global.exception.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

	// === 400 BAD REQUEST ===

	DUPLICATE_EMAIL(
		HttpStatus.BAD_REQUEST,
		"이미 존재하는 이메일입니다.",
		"MEMBER_ERROR_400_DUPLICATE_EMAIL"
	),

	INVALID_ONBOARDING_DATA(
		HttpStatus.BAD_REQUEST,
		"온보딩 데이터가 유효하지 않습니다.",
		"CLIENT_ERROR_400_INVALID_ONBOARDING_DATA"
	),

	INVALID_MEMBER_ID(
		HttpStatus.BAD_REQUEST,
		"멤버 ID가 유효하지 않습니다.",
		"MEMBER_ERROR_400_INVALID_MEMBER_ID"
	),

	// === 401 UNAUTHORIZED ===

	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,
		"비밀번호가 올바르지 않습니다.",
		"MEMBER_ERROR_401_INVALID_PASSWORD"
	),

	// === 403 FORBIDDEN ===

	INVALID_ACCOUNT_STATUS(
		HttpStatus.FORBIDDEN,
		"계정 상태가 유효하지 않습니다.",
		"MEMBER_ERROR_403_INVALID_ACCOUNT_STATUS"
	),

	// === 404 NOT FOUND ===,
	MEMBER_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"사용자를 찾을 수 없습니다.",
		"MEMBER_ERROR_404_NOT_FOUND"
	),

	SOCIAL_ACCOUNT_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"소셜 계정을 찾을 수 없습니다.",
		"MEMBER_ERROR_404_SOCIAL_ACCOUNT_NOT_FOUND"
	),

	// === 409 CONFLICT ===,

	ONBOARDING_NOT_ALLOWED(
		HttpStatus.CONFLICT,
		"온보딩을 완료할 수 없는 상태입니다.",
		"CLIENT_ERROR_409_ONBOARDING_NOT_ALLOWED"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;

}
