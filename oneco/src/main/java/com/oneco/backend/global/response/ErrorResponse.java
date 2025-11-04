package com.oneco.backend.global.response;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse extends BaseResponse {

	private final String message;
	private final String code; // 도메인 별 세부 오류 코드를 위한 필드
	private final Map<String, Object> reasons;

	public ErrorResponse(
		HttpStatus status,
		String message,
		String code,
		Map<String, Object> reasons
	) {
		super(status);
		this.message = message;
		this.code = code;
		this.reasons = reasons;
	}

	public static ErrorResponse from(ErrorCode errorCode) {
		HttpStatus status = errorCode.getHttpStatus();
		String message = errorCode.getMessage();
		String code = errorCode.getCode();

		return new ErrorResponse(status, message, code, null);
	}

	public static ErrorResponse of(ErrorCode errorCode, Map<String, Object> reasons) {
		HttpStatus status = errorCode.getHttpStatus();
		String message = errorCode.getMessage();
		String code = errorCode.getCode();

		return new ErrorResponse(status, message, code, reasons);
	}

	public static ErrorResponse of(HttpStatus status, String message, String code) {

		return new ErrorResponse(status, message, code, null);
	}

}
