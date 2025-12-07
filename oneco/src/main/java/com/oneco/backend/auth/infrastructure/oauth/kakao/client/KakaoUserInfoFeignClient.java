package com.oneco.backend.auth.infrastructure.oauth.kakao.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.oneco.backend.auth.infrastructure.oauth.kakao.config.KakaoApiFeignConfig;
import com.oneco.backend.auth.infrastructure.oauth.kakao.dto.KakaoUserInfoResponse;

@FeignClient(
	url="${kakao.kapi.base-url}",
	name = "KakaoUserInfoFeignClient",
	configuration = KakaoApiFeignConfig.class
)
public interface KakaoUserInfoFeignClient {

	@GetMapping("/v2/user/me")
	KakaoUserInfoResponse getUserInfo(
		// Authorization: Bearer {accessToken} 헤더를 그대로 넘겨주기 위해 @RequestHeader 사용
		// KakaoOAuthClient에서 "Bearer" + kakaoAccessToken 을 붙여서 보내야함
		@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
	);
}
