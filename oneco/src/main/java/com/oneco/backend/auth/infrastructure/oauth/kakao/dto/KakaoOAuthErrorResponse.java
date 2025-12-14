package com.oneco.backend.auth.infrastructure.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



/**
 * Kakao 에러 응답(JSON) 파싱용 DTO.
 * <p>
 * Kakao 토큰 발급 실패 응답 예시:
 * <p>
 * HTTP 400
 * {
 * "error": "invalid_grant",
 * "error_description": "authorization code not found for this user"
 * }
 * <p>
 * Kakao API 호출 실패 응답 예시:
 * <p>
 * HTTP 401
 * {
 * "error": "invalid_token",
 * "error_description": "access token expired"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record KakaoOAuthErrorResponse(String error, String errorDescription) {
}