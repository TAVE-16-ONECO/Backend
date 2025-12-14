package com.oneco.backend.auth.infrastructure.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// JSON에 내가 모르는 필드가 있어도 무시
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoTokenResponse(
	@JsonProperty("access_token")
	String accessToken,

	@JsonProperty("refresh_token")
	String refreshToken,

	@JsonProperty("expires_in")
	Long expiresIn,

	@JsonProperty("token_type")
	String tokenType,

	@JsonProperty("id_token")
	String idToken,

	@JsonProperty("scope")
	String scope
) {
}