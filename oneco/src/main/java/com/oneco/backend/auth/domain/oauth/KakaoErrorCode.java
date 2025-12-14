package com.oneco.backend.auth.domain.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum KakaoErrorCode implements ErrorCode {

	// ============================
	// 1. 인가 코드 / 요청 문제 (400)
	// ============================

	INVALID_AUTH_CODE(
		HttpStatus.BAD_REQUEST,
		"유효하지 않은 카카오 인가 코드입니다.",
		"KAKAO_400_INVALID_AUTH_CODE"
	),

	INVALID_REQUEST(
		HttpStatus.BAD_REQUEST,
		"유효하지 않은 카카오 OAuth 요청입니다.",
		"KAKAO_400_INVALID_REQUEST"
	),

	// ============================
	// 2. 클라이언트 인증 문제 (401)
	// ============================

	INVALID_CLIENT(
		HttpStatus.UNAUTHORIZED,
		"유효하지 않은 카카오 클라이언트 정보입니다.",
		"KAKAO_401_INVALID_CLIENT"
	),

	OIDC_AUTH_FAILED(
		HttpStatus.UNAUTHORIZED,
		"카카오 OIDC 인증에 실패했습니다.",
		"KAKAO_OIDC_401_AUTH_FAILED"
	),

	/**
	 * 액세스 토큰이 만료 or 잘못된 경우
	 * * - 401 + error=invalid_token (API 호출 시)
	 */
	INVALID_ACCESS_TOKEN(
		HttpStatus.UNAUTHORIZED,
		"유효하지 않은 카카오 액세스 토큰입니다.",
		"KAKAO_401_INVALID_ACCESS_TOKEN"),

	// ============================
	// 3. 공통 서버/파싱 에러
	// ============================

	SERVER_ERROR(
		HttpStatus.BAD_GATEWAY,
		"카카오 서버 오류로 요청을 처리할 수 없습니다.",
		"KAKAO_502_SERVER_ERROR"
	),

	RESPONSE_PARSING_FAILED(
		HttpStatus.BAD_GATEWAY,
		"카카오 에러 응답을 해석할 수 없습니다.",
		"KAKAO_502_RESPONSE_PARSING_FAILED"
	),

	// ============================
	// 4. UserInfo (/v2/user/me) 관련
	// ============================

	USERINFO_REQUEST_FAILED(
		HttpStatus.BAD_GATEWAY,
		"카카오 사용자 정보 조회에 실패했습니다.",
		"KAKAO_502_USERINFO_REQUEST_FAILED"
	),

	USERINFO_UNAUTHORIZED(
		HttpStatus.UNAUTHORIZED,
		"카카오 사용자 정보 조회 권한이 없습니다.",
		"KAKAO_401_USERINFO_UNAUTHORIZED"
	),

	USERINFO_FORBIDDEN(
		HttpStatus.FORBIDDEN,
		"카카오 사용자 정보 접근 권한이 없습니다.",
		"KAKAO_403_USERINFO_FORBIDDEN"
	),

	USERINFO_RATE_LIMIT_EXCEEDED(
		HttpStatus.TOO_MANY_REQUESTS,
		"카카오 사용자 정보 조회 호출 한도를 초과했습니다.",
		"KAKAO_429_USERINFO_RATE_LIMIT_EXCEEDED"
	),

	USERINFO_SERVER_ERROR(
		HttpStatus.BAD_GATEWAY,
		"카카오 사용자 정보 서버 에러가 발생했습니다.",
		"KAKAO_502_USERINFO_SERVER_ERROR"
	),

	// ============================
	// 5. Fallback
	// ============================

	OAUTH_FAILED(
		HttpStatus.BAD_GATEWAY,
		"카카오 OAuth 연동에 실패했습니다.",
		"KAKAO_502_OAUTH_FAILED"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}