package com.oneco.backend.global.exception;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

	private final HttpStatus httpStatus;
	private final String code;

	protected BaseException(String message, HttpStatus httpStatus, String code) {
		super(message);
		this.httpStatus = httpStatus;
		this.code = code;
	}

	public static BaseException from(ErrorCode errorCode) {
		return new BaseException(errorCode.getMessage(), errorCode.getHttpStatus(), errorCode.getCode()) {
		};
	}
}

