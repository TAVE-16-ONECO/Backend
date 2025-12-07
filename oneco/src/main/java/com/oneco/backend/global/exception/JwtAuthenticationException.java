package com.oneco.backend.global.exception;

import org.springframework.security.core.AuthenticationException;

import com.oneco.backend.global.exception.constant.JwtErrorCode;

public class JwtAuthenticationException extends AuthenticationException {
	private final JwtErrorCode errorCode;

	public JwtAuthenticationException(JwtErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public JwtErrorCode getErrorCode() {
		return errorCode;
	}
}
