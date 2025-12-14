package com.oneco.backend.auth.infrastructure.oauth.kakao.client;

import org.springframework.stereotype.Component;

import com.oneco.backend.auth.domain.oauth.KakaoErrorCode;
import com.oneco.backend.auth.infrastructure.oauth.kakao.dto.KakaoTokenResponse;
import com.oneco.backend.auth.infrastructure.oauth.kakao.config.KakaoOAuthProperties;
import com.oneco.backend.auth.infrastructure.oauth.kakao.dto.KakaoUserInfoResponse;
import com.oneco.backend.global.exception.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoOAuthClient {

	private final KakaoOAuthProperties kakaoOAuthProperties;
	private final KakaoAuthFeignClient kakaoAuthFeignClient;
	private final KakaoUserInfoFeignClient kakaoUserInfoFeignClient;

	public KakaoTokenResponse requestAccessToken(String authorizationCode) {

		log.info("Requesting Kakao access token with authorization code: {}", authorizationCode);
		log.info("Kakao OAuth Properties - Client ID: {}, Client Secret: {}, Redirect URI: {}",
			kakaoOAuthProperties.getClientId(),
			kakaoOAuthProperties.getClientSecret(),
			kakaoOAuthProperties.getRedirectUri()
		);

		KakaoTokenResponse response = kakaoAuthFeignClient.requestAccessToken(
			"authorization_code",
			kakaoOAuthProperties.getClientId(),
			kakaoOAuthProperties.getClientSecret(),
			kakaoOAuthProperties.getRedirectUri(),
			authorizationCode
		);
		log.info("Kakao Token Response received: {}", response);

		if (response == null || response.accessToken() == null) {
			throw BaseException.from(KakaoErrorCode.OAUTH_FAILED);
		}

		return response;
	}

	public KakaoUserInfoResponse requestUserInfo(String accessToken) {
		// 1) Authorization 헤더 생성
		String authorizationHeader = "Bearer " + accessToken;

		log.info(
			"[KakaoOAuthClient] Requesting Kakao user info. token prefix: {}**** (length={})",
			accessToken.substring(0, Math.min(6, accessToken.length())),
			accessToken.length()
		);

		KakaoUserInfoResponse response = kakaoUserInfoFeignClient.getUserInfo(authorizationHeader);

		// 방어 코드
		if (response == null) {
			log.error("[KakaoOAuthClient] Kakao user info response is null");
			throw BaseException.from(KakaoErrorCode.OAUTH_FAILED);
		}

		log.info("[KakaoOAuthClient] Kakao User Info Response received: {}", response);
		return response;
	}
}
