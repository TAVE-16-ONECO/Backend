package com.oneco.backend.auth.infrastructure.oauth.kakao.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.oneco.backend.auth.infrastructure.oauth.kakao.config.KakaoOAuthFeignConfig;
import com.oneco.backend.auth.infrastructure.oauth.kakao.dto.KakaoTokenResponse;

@FeignClient(
	name= "KakaoAuthFeignClient",    // 스프링 컨테이너에 등록될 빈 이름
	url =  "${kakao.oauth.base-url}",  // 카카오 인증 서버의 베이스 url
	configuration = KakaoOAuthFeignConfig.class
)
public interface KakaoAuthFeignClient {

	// POST /oauth/token 엔드포인트
	@PostMapping("/oauth/token")
	public KakaoTokenResponse requestAccessToken(
		@RequestParam("grant_type") String grantType,  // 필수: grant_type = authorization_code
		@RequestParam("client_id") String clientId,    // 필수: REST API key(카카오 앱의 client_id)
		@RequestParam(value="client_secret", required = false) String clientSecret, // 선택: 보안용 secret
		@RequestParam("redirect_uri") String redirectUri, // 필수: 인가 코드 받을 때 사용한 redirect_uri와 동일해야 함
		@RequestParam("code") String code    // 필수: 카카오가 리다이렉트로 넘겨준 authorization_code
	);
}
