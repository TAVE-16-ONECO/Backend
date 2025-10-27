package com.oneco.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;

@Getter
@JsonPropertyOrder({"success", "httpStatus", "code", "message", "data", "errors"})
public class BaseResponse<T> {

	@JsonProperty("success")
	private final boolean success;

	@JsonProperty("httpStatus")
	private final int httpStatus;

	@JsonProperty("code")
	private final String code;

	@JsonProperty("message")
	private final String message;
	/**
	 * 실패 시 추가적인 상세 오류 정보.
	 *
	 * <p>필드 단위 검증 오류 등 복수의 에러를 담을 수 있으며, 성공 응답일 경우 보통 {@code null}입니다.</p>
	 *
	 * <p>예시:
	 * <pre>{@code
	 * {
	 *   "email": "이메일 형식이 올바르지 않습니다.",
	 *   "password": "비밀번호는 8자 이상이어야 합니다."
	 * }
	 * }</pre>
	 * </p>
	 *
	 * <p>{@code null}인 경우 {@link com.fasterxml.jackson.annotation.JsonInclude} 설정으로 JSON 응답에서 해당 필드가 제외됩니다.</p>
	 */
	@JsonProperty("errors")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final Object errors;
	/**
	 * 응답 페이로드 데이터(성공 시 포함).
	 *
	 * 예시:
	 * <pre>{@code
	 * "data": { "id": 1, "name": "Alice" }
	 * }</pre>
	 *
	 * null인 경우 @JsonInclude(JsonInclude.Include.NON_NULL) 설정으로 JSON 응답에서 해당 필드가 제외됩니다.
	 */
	@JsonProperty("data")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T data;

	// 1. 성공
	// 응답만을 위한 생성자
	private BaseResponse(T data) {
		this.success = BaseResponseStatus.OK.isSuccess();
		this.httpStatus = BaseResponseStatus.OK.getHttpStatus();
		this.code = BaseResponseStatus.OK.getCode();
		this.message = BaseResponseStatus.OK.getMessage();
		this.data = data;
		this.errors = null;
	}

	// 1-1. 성공 + 커스텀 메시지(선택)
	private BaseResponse(T data, String customMessage) {
		this.success = BaseResponseStatus.OK.isSuccess();
		this.httpStatus = BaseResponseStatus.OK.getHttpStatus();
		this.code = BaseResponseStatus.OK.getCode();
		this.message = customMessage;
		this.data = data;
		this.errors = null;
	}

	// 1. 실패(단순)
	// data + error 없음 (기본)
	private BaseResponse(BaseResponseStatus status) {
		this.success = status.isSuccess();
		this.httpStatus = status.getHttpStatus();
		this.code = status.getCode();
		this.message = status.getMessage();
		this.data = null;
		this.errors = null;
	}

	// 2. 실패(상세)
	// error, 커스텀 메세지는 선택사항
	private BaseResponse(BaseResponseStatus status, Object errors, String customMessage) {
		this.success = status.isSuccess();
		this.httpStatus = status.getHttpStatus();
		this.code = status.getCode();
		this.message = (customMessage != null ? customMessage : status.getMessage());
		this.data = null;
		this.errors = errors;
	}

	// =========== 정적 헬퍼 메서드 ===========

	// 성공 응답 생성 (데이터 없음)
	public static BaseResponse<Void> ok() {
		return new BaseResponse<>(null);
	}

	// 성공 응답 생성 (데이터 포함)
	public static <T> BaseResponse<T> ok(T data) {
		return new BaseResponse<>(data);
	}

	// 성공 응답 생성 + 커스텀 메시지 (데이터 없음)
	public static BaseResponse<Void> okMessage(String customMessage) {
		return new BaseResponse<>(null, customMessage);
	}

	// 성공 응답 생성 + 커스텀 메시지 (데이터 포함)
	public static <T> BaseResponse<T> ok(T data, String customMessage) {
		return new BaseResponse<>(data, customMessage);
	}

	// 실패 응답 생성 (단순)
	public static BaseResponse<Void> error(BaseResponseStatus status) {
		return new BaseResponse<>(status);
	}

	// 실패 응답 생성 + 상세 에러 포함
	public static BaseResponse<Void> error(BaseResponseStatus status, Object errors) {
		return new BaseResponse<>(status, errors, null);
	}

	// 실패 응답 생성 + 상세 에러 + 커스텀 메시지 입력
	public static BaseResponse<Void> error(BaseResponseStatus status, Object errors, String customMessage) {
		return new BaseResponse<>(status, errors, customMessage);
	}
}
