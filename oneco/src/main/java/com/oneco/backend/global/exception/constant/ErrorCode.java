package com.oneco.backend.global.exception.constant;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	String getCode();

	String getMessage();

	HttpStatus getHttpStatus();

}
