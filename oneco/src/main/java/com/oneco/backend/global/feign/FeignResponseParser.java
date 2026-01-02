package com.oneco.backend.global.feign;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import feign.Util;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeignResponseParser {

	/**
	  	 * @param response   Feign 이 전달해 준 HTTP 응답 객체 (status, headers, body 포함)
	 	 * @param objectMapper  스프링이 관리하는 ObjectMapper (전역 설정 적용됨)
	  	 * @param dtoClass   JSON 을 역직렬화할 대상 DTO 타입
	     *                    - 예: KakaoOAuthErrorResponse.class
	 	 *                    - 예: KakaoApiErrorResponse.class
	 	 * @param <T>        DTO 제네릭 타입
	 */
	public static <T> ParsedBody<T> parseBody(
		Response response,
		ObjectMapper objectMapper,
		Class<T> dtoClass
	)throws IOException {

		// 응답 바디가 없는 경우 : rawBody = "", body = null
		if(response.body() == null){
			return new ParsedBody<>("",null);
		}


		// Feign Util을 사용해서 바디를 문자열로 읽어온다.
		//예시 rawBody:
		//   "{\"error\":\"invalid_grant\",\"error_description\":\"authorization code not found for this user\"}"
		String rawBody = Util.toString(response.body().asReader());

		// ObjectMapper로 JSON -> DTO로 역직렬화
		///   dtoClass 가 KakaoOAuthErrorResponse.class 인 경우:
		// 	   - body.error() == "invalid_grant"
		//     - body.errorDescription() == "authorization code not found for this user"
		T body = objectMapper.readValue(rawBody, dtoClass);

		// 원문(rawBody) + DTO(body) 를 함께 감싸서 돌려준다.
		//    ErrorDecoder 에서는:
		//      - rawBody 는 에러 로그에 남기고
		//      - body 는 KakaoErrorCode 매핑에 사용하게 된다.
		return new ParsedBody<>(rawBody, body);
	}

	/**
	 * Feign 응답 바디 파싱 결과를 담는 record.
	 *
	 * @param rawBody 응답 바디 원문 문자열
	 * @param body    DTO 로 역직렬화된 객체
	 */
	public record ParsedBody<T>(String rawBody, T body){}
}
