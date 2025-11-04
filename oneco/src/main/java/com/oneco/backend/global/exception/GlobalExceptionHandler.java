package com.oneco.backend.global.exception;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.oneco.backend.global.exception.constant.GlobalErrorCode;
import com.oneco.backend.global.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 도메인별 커스텀 예외 처리
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<Object> handleBaseException(BaseException e) {

		log.info("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);

		return ResponseEntity
			.status(e.getHttpStatus())
			.body(ErrorResponse.of(e.getHttpStatus(), e.getMessage(), e.getCode()));
	}

	// 검증 예외 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		BindingResult br = e.getBindingResult();

		// 필드 에러
		Map<String, Object> fieldErrors = new LinkedHashMap<>();

		br.getFieldErrors().forEach(error -> {
			String fieldName = error.getField();
			String message = error.getDefaultMessage();
			Object existing = fieldErrors.get(fieldName);

			if (existing == null) {
				// 첫 에러 메시지 단건 등록
				fieldErrors.put(fieldName, message);
			} else if (existing instanceof List<?> list) {
				// 이미 리스트인 경우 -> 추가
				((List<String>)list).add(message);
			} else {
				// 기존 단건 문자열을 리스트로 변경
				fieldErrors.put(fieldName, new ArrayList<>(List.of((String)existing, message)));
			}
		});

		// 글로벌 에러
		List<String> globalErrors = br.getGlobalErrors().stream()
			.map(ObjectError::getDefaultMessage)
			.toList();

		// 사용자 입력 오류이므로 warning 레벨로 로깅
		if (!fieldErrors.isEmpty())
			log.warn("[ValidationException:Field] {}", fieldErrors);
		if (!globalErrors.isEmpty())
			log.warn("[ValidationException:Global] {}", globalErrors);

		// 응답 데이터 구성
		Map<String, Object> errors = new LinkedHashMap<>();
		if (!fieldErrors.isEmpty())
			errors.put("fieldErrors", fieldErrors);
		if (!globalErrors.isEmpty())
			errors.put("globalErrors", globalErrors);

		// 응답 생성
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(GlobalErrorCode.VALIDATION_ERROR, errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleAllException(Exception e) {

		log.error("Unhandled exception occurred: {}", e.getMessage(), e);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ErrorResponse.from(GlobalErrorCode.INTERNAL_SERVER_ERROR));
	}
}
