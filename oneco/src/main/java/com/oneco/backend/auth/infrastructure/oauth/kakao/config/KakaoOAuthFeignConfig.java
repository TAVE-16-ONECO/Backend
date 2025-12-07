package com.oneco.backend.auth.infrastructure.oauth.kakao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneco.backend.auth.domain.oauth.KakaoErrorCode;
import com.oneco.backend.auth.infrastructure.oauth.kakao.dto.KakaoOAuthErrorResponse;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.feign.FeignResponseParser;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 역할:
 *  - KakaoAuthFeignClient 전용 Feign 설정 클래스
 *  - 카카오 서버가 4xx / 5xx 에러를 반환했을 때,
 *  - 서비스 레이어는 FeignException, HTTP 상태 코드, 카카오 error 문자열 등을 직접 보지 않고,
 *    KakaoErrorCode 중심으로만 에러를 처리할 수 있게 해준다.
 * 전체 동작 흐름
 * 1. 애플리케이션 기동 시
 *    - @Configuration 이기 때문에 스프링이 이 클래스를 스캔한다.
 *    - @Bean
 *
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class KakaoOAuthFeignConfig {

	/**
	 *카카오 에러 응답(JSON)을 파싱하기 위한 ObjectMapper
	 * 카카오 에러 응답 예시:
	 * {
	 *     "error": "invalid_grant",
	 *     "error_description": "authorization code not found for this user"
	 * }
	 */
	private final ObjectMapper objectMapper;

	@Bean
	public ErrorDecoder kakaoErrorDecoder() {
		return new ErrorDecoder() {
			/**
			 * @Param methodKey
			 * - 예: KakaoAuthFeignClient#requestAccessToken(String,String, String,String,String)
			 * - 어떤 Feign 메서드에서 에러가 났는지 구분하는데 사용 가능
			 * @Param response
			 * - 카카오 서버로부터 받은 HTTP 응답 전체 (status, headers,body 포함)
			 * @return
			 * - AppException(KakaoErrorCode.xxx) 반환
			 */
			@Override
			public Exception decode(String methodKey, Response response) {
				// rawBody = “JSON 파싱하기 전, 그대로 읽어온 응답 바디 문자열”
				// 예: "{\"error\":\"invalid_token\",\"error_description\":\"access token expired\"}"
				String rawBody = "";
				String kakaoError = null;
				String kakaoErrorDescription = null;

				try {
					// 공통 파서 유틸을 사용해 rawBody + DTO 를 한 번에 얻는다.
					FeignResponseParser.ParsedBody<KakaoOAuthErrorResponse> parsed =
						FeignResponseParser.parseBody(response, objectMapper, KakaoOAuthErrorResponse.class);

					rawBody = parsed.rawBody();
					KakaoOAuthErrorResponse errorResponse = parsed.body();

					if (errorResponse != null) {
						kakaoError = errorResponse.error();
						kakaoErrorDescription = errorResponse.errorDescription();
					}
				} catch (Exception e) {
					//JSON 파싱 중 예외가 발생하는 경우:
					// 이 경우, 카카오, 에러 내용을 세밀하게 분석할 수 없기 때문에
					// KakaoErrorCode.RESPONSE_PARSING_FAILED로 래핑하여 던진다.
					log.warn(
						"[KAKAO-OAUTH] failed to parse error response body. " +
							"methodKey: {}, status: {}, rawBody: {}",
						methodKey,
						response.status(),
						rawBody,
						e
					);
					return BaseException.from(KakaoErrorCode.RESPONSE_PARSING_FAILED);
				}
				// 공통 에러 로그:
				// - methodKey         : 어느 Feign 메서드에서 발생했는지
				// - status            : HTTP 상태 코드 (400, 401, 403, 429, 500 등)
				// - kakaoError        : 카카오 error 필드 (invalid_grant, invalid_request, invalid_client, ...)
				// - kakaoErrorDesc... : 카카오 error_description (자세한 설명)
				// - rawBody           : 전체 응답 바디 (추후 디버깅/모니터링을 위해)
				log.error(
					"[KAKAO-OAUTH] Feign error - methodKey: {}, status: {}, error: {}, description: {}, rawBody: {}",
					methodKey,
					response.status(),
					kakaoError,
					kakaoErrorDescription,
					rawBody
				);

				int status = response.status();
				// =====================================================================
				// 1. 400 Bad Request: 인가 코드/요청 파라미터 관련 문제
				// =====================================================================
				if (status == 400) {


					// 사례 1) 잘못된 authorization_code 사용
					// → 사용자는 카카오 로그인 화면에서 다시 로그인을 시도해야 하는 케이스
					if ("invalid_grant".equals(kakaoError)) {
						return BaseException.from(KakaoErrorCode.INVALID_AUTH_CODE);
					}

					//  사례 2) redirect_uri 및 요청 형식 문제
					//  → 서버 설정이나 프론트 요청 값이 잘못된 경우
					if ("invalid_request".equals(kakaoError)) {
						return BaseException.from(KakaoErrorCode.INVALID_REQUEST);
					}

					// 그 외 400 → 구체적인 error 값이 없거나,
					// 별도 분기하지 않는 케이스는 INVALID_REQUEST 로 통일해 처리.
					return BaseException.from(KakaoErrorCode.INVALID_REQUEST);
				}

				// =====================================================================
				// 2. 401 Unauthorized: 클라이언트 인증/토큰 관련 문제
				// =====================================================================
				if(status == 401) {
					// 사례 3) 잘못된 client_id / client_secret
					// -> REST API 키를 잘못 입력했거나, client_secret이 틀린 경우
					if("invalid_client".equals(kakaoError)){
						return BaseException.from(KakaoErrorCode.INVALID_CLIENT);
					}

					// 그 외 401 -> 모두 토큰 관련 문제로 보고 INVALID_CLIENT으로 처리
					return BaseException.from(KakaoErrorCode.INVALID_CLIENT);
				}

				// =====================================================================
				// 3. 5xx Server Error: 카카오 인증 서버 내부 장애
				// =====================================================================
				if (status >= 500 && status < 600) {

					/*
					 * 사례 4) 카카오 인증 서버 내부 에러
					 */
					return BaseException.from(KakaoErrorCode.SERVER_ERROR);
				}

				// =====================================================================
				// 4. 그 외 예외적인 상태 코드: 모두 포괄적인 OAuth 실패로 처리
				// =====================================================================
				/*
				 * 토큰 엔드포인트에서 일반적으로 기대하지 않는 상태 코드이므로
				 * 포괄적인 KakaoErrorCode.OAUTH_FAILED 로 처리해 둔다.
				 */
				return BaseException.from(KakaoErrorCode.OAUTH_FAILED);
			}

		};
	}
}
