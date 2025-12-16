package com.oneco.backend.global.response;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataResponse<T> extends BaseResponse {

	private final T data;

	public DataResponse(HttpStatus status, T data) {
		super(status);
		this.data = data;
	}

	public static <T> DataResponse<T> from(T data) {
		return new DataResponse<>(HttpStatus.OK, data);
	}

	public static <T> DataResponse<T> ok() {
		return new DataResponse<>(HttpStatus.OK, null);
	}


}
