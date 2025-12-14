package com.oneco.backend.global.exception.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

	// 400 Bad Request
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다.", "CLIENT_ERROR_400_INVALID_REQUEST"),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다.", "CLIENT_ERROR_400_VALIDATION_ERROR"),
	MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "필수 요청 값이 누락되었습니다.", "CLIENT_ERROR_400_MISSING_PARAMETER"),
	TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다.", "CLIENT_ERROR_400_TYPE_MISMATCH"),
	NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "응답을 제공할 수 없는 형식입니다.", "CLIENT_ERROR_406_NOT_ACCEPTABLE"),
	PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "요청 본문이 허용 용량을 초과했습니다.", "CLIENT_ERROR_413_PAYLOAD_TOO_LARGE"),

	// 401 Unauthorized
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", "CLIENT_ERROR_401_UNAUTHORIZED"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다.", "CLIENT_ERROR_401_INVALID_TOKEN"),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 인증 토큰입니다.", "CLIENT_ERROR_401_EXPIRED_TOKEN"),
	INVALID_OAUTH_STATE(HttpStatus.UNAUTHORIZED, "유효하지 않은 OAuth 상태 (state) 값입니다. 다시 로그인 요청을 시도해 주세요.","AUTH_401_INVALID_OAUTH_STATE"),
	// 403 Forbidden
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", "CLIENT_ERROR_403_FORBIDDEN"),

	// 404 Not Found
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", "CLIENT_ERROR_404_NOT_FOUND"),
	ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 API 경로입니다.", "CLIENT_ERROR_404_ENDPOINT_NOT_FOUND"),

	// 405 Method Not Allowed
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다.", "CLIENT_ERROR_405_METHOD_NOT_ALLOWED"),


	// 409 Conflict
	CONFLICT(HttpStatus.CONFLICT, "요청이 현재 리소스 상태와 충돌합니다.", "CLIENT_ERROR_409_CONFLICT"),
	DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다.", "CLIENT_ERROR_409_DUPLICATE_RESOURCE"),

	// 415 Unsupported Media Type
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 콘텐츠 타입입니다.",
		"CLIENT_ERROR_415_UNSUPPORTED_MEDIA_TYPE"),

	// 429 Too Many Requests
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다.", "CLIENT_ERROR_429_TOO_MANY_REQUESTS"),

	// 5xx Server Errors
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.", "SERVER_ERROR_500_INTERNAL_SERVER_ERROR"),
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다.", "SERVER_ERROR_500_DATABASE_ERROR"),
	BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "게이트웨이 오류가 발생했습니다.", "SERVER_ERROR_502_BAD_GATEWAY"),
	EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 통신 오류입니다.", "SERVER_ERROR_502_EXTERNAL_API_ERROR"),
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서비스를 이용할 수 없습니다.", "SERVER_ERROR_503_SERVICE_UNAVAILABLE"),
	GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "게이트웨이 시간 초과입니다.", "SERVER_ERROR_504_GATEWAY_TIMEOUT"),
	;

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
