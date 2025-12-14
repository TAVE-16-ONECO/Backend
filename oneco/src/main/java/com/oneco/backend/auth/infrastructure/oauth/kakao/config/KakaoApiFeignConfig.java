package com.oneco.backend.auth.infrastructure.oauth.kakao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneco.backend.auth.domain.oauth.KakaoErrorCode;
import com.oneco.backend.auth.infrastructure.oauth.kakao.dto.KakaoApiErrorResponse;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.feign.FeignResponseParser;
import com.oneco.backend.global.feign.FeignResponseParser.ParsedBody;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 역할:
 * - KakaoUserInfoFeignClient 등 KAPI(https://kapi.kakao.com) 전용 Feign 설정 클래스
 * - 카카오 사용자 정보 조회 등에서 4xx / 5xx 에러가 발생했을 때,
 * 카카오 API 에러 JSON(code, msg)을 파싱해 우리 도메인 예외로 변환한다.
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class KakaoApiFeignConfig {

	private final ObjectMapper objectMapper;

	@Bean
	public ErrorDecoder kakaoApiErrorDecoder() {
		return new ErrorDecoder() {

			@Override
			public Exception decode(String methodKey, Response response) {
				String rawBody = "";
				Integer kakaoCode = null;
				String kakaoMsg = null;

				try {
					// 공통 파서 유틸을 사용해 rawBody + KakaoApiErrorResponse 를 얻는다.
					ParsedBody<KakaoApiErrorResponse> parsed =
						FeignResponseParser.parseBody(response, objectMapper, KakaoApiErrorResponse.class);

					rawBody = parsed.rawBody();
					KakaoApiErrorResponse errorResponse = parsed.body();

					if (errorResponse != null) {
						kakaoCode = errorResponse.code();
						kakaoMsg = errorResponse.msg();
					}
				} catch (Exception e) {
					log.warn(
						"[KAKAO-API] failed to parse error response body. " +
							"methodKey: {}, status: {}, rawBody: {}",
						methodKey,
						response.status(),
						rawBody,
						e
					);
					return BaseException.from(KakaoErrorCode.RESPONSE_PARSING_FAILED);
				}

				log.error(
					"[KAKAO-API] Feign error - methodKey: {}, status: {}, kakaoCode: {}, msg: {}, rawBody: {}",
					methodKey,
					response.status(),
					kakaoCode,
					kakaoMsg,
					rawBody
				);

				int status = response.status();

				// =====================================================================
				// 0. 400 Bad Request: 잘못된 요청(파라미터, 형식 등)
				// =====================================================================
				// 예)
				//   - 잘못된 쿼리 파라미터
				//   - 필수 파라미터 누락
				//   - 지원하지 않는 값 등
				if (status == 400) {
					// Kakao API 문서 기준으로, kakaoCode 에 따라 더 세분화하고 싶으면 여기서 분기 가능
					// 예: code == -2 → 잘못된 파라미터, code == -3 → 지원하지 않는 API 등 (가정)
					return BaseException.from(KakaoErrorCode.INVALID_REQUEST);
				}

				// =====================================================================
				// 1. 401 Unauthorized: 액세스 토큰 문제(유효하지 않음, 만료 등)
				// =====================================================================
				// 예)
				//   - access token 이 존재하지 않거나
				//   - 만료되었거나
				//   - 형식이 잘못된 경우
				if (status == 401) {
					// kakaoCode 를 사용할 수 있다면, 더 구체적으로 나눈다.
					// 아래 코드는 예시이고, 실제 코드는 Kakao 문서의 code 값에 맞춰 조정해야 한다.
					if (kakaoCode != null) {
						switch (kakaoCode) {
							case -401 -> {
								// 예: "this access token does not exist"
								return BaseException.from(KakaoErrorCode.INVALID_ACCESS_TOKEN);
							}
							case -402 -> {
								// 예: 만료된 토큰 (가정)
								return BaseException.from(KakaoErrorCode.OAUTH_FAILED);
							}
							default -> {
								// 정의하지 않은 code 값은 포괄적으로 처리
								return BaseException.from(KakaoErrorCode.OAUTH_FAILED);
							}
						}
					}
					// kakaoCode 를 못 읽은 경우에도, 토큰 관련 문제로 보고 포괄 처리
					return BaseException.from(KakaoErrorCode.OAUTH_FAILED);
				}

				// =====================================================================
				// 2. 403 Forbidden: 권한 부족(스코프 부족, 동의 안 된 리소스 접근 등)
				// =====================================================================
				// 예)
				//   - 로그인은 되어 있지만, 해당 API 에 필요한 동의를 받지 못한 경우
				//   - ex) 친구 목록, 메시지 전송 등 추가 동의 필요 API
				if (status == 403) {
					return BaseException.from(KakaoErrorCode.OAUTH_FAILED);
				}

				// =====================================================================
				// 3. 404 Not Found: 존재하지 않는 리소스
				// =====================================================================
				// 예)
				//   - 요청한 리소스(유저, 리소스 id 등)가 삭제되었거나 존재하지 않는 경우
				if (status == 404) {
					return BaseException.from(KakaoErrorCode.OAUTH_FAILED);
				}

				// =====================================================================
				// 4. 429 Too Many Requests: 레이트 리밋 초과
				// =====================================================================
				// 예)
				//   - 카카오가 정해둔 호출 제한을 초과한 경우
				//   - 잠시 후 재시도 필요
				if (status == 429) {
					return BaseException.from(KakaoErrorCode.OAUTH_FAILED);
				}

				// =====================================================================
				// 5. 5xx: 카카오 API 서버 장애
				// =====================================================================
				// 예)
				//   - 카카오 내부 서버 에러
				//   - 일시적인 장애, 배포 중 문제 등
				if (status >= 500 && status < 600) {
					return BaseException.from(KakaoErrorCode.SERVER_ERROR);
				}

				// =====================================================================
				// 6. 그 외 예외적인 상태 코드: 모두 포괄적인 실패로 처리
				// =====================================================================
				// 예)
				//   - 예상하지 못한 상태 코드 (예: 3xx, 418, 기타 특이한 케이스)
				//   - 카카오 문서 상에 명시되지 않은 값이 들어오는 경우
				return BaseException.from(KakaoErrorCode.OAUTH_FAILED);
			}
		};
	}
}
