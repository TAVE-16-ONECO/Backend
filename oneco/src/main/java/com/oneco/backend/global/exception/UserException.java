package com.oneco.backend.global.exception;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.UserErrorCode;

import lombok.Getter;

@Getter
public class UserException extends BaseException {

	private UserException(String message, HttpStatus httpStatus, String code) {
		super(message, httpStatus, code);
	}

	public static UserException from(UserErrorCode errorCode) {
		return new UserException(errorCode.getMessage(), errorCode.getHttpStatus(), errorCode.getCode());
	}
}
