package com.oneco.backend.global.response;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

	OK(true, 200, "200", "요청에 성공하였습니다."),

	BAD_REQUEST(false, 400, "400", "잘못된 요청입니다."),

	INTERNAL_SERVER_ERROR(false, 500, "500", "서버 내부 오류입니다.");

	private final boolean success;
	private final int httpStatus;
	private final String code;

	private final String message;

	BaseResponseStatus(boolean success, int httpStatus, String code, String message) {
		this.success = success;
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
