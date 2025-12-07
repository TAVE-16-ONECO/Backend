package com.oneco.backend.auth.infrastructure.oauth.kakao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Kakao OIDC 관련 설정값.
 *
 * - issuer: Kakao OIDC 토큰 발급자(iss 클레임) 값. 보통 https://kauth.kakao.com
 * - jwkSetUri: Kakao 공개키(JWK) 목록이 노출되는 주소.
 *   (Kakao docs: https://kauth.kakao.com/.well-known/jwks.json)
 *
 * application.yml 에서 kakao.oidc.* 로 주입받는다.
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix= "kakao.oidc")
public class KakaoOIDCProperties {

	/**
	 * ID 토큰 발급자(issuer).
	 * - ID 토큰의 iss 클레임과 반드시 일치해야 한다.
	 *   예) https://kauth.kakao.com
	 */
	private String issuer;

	/**
	 * Kakao OIDC 공개키 JWK Set URI.
	 * - RS256 등으로 서명된 ID 토큰의 서명을 검증할 때 사용된다.
	 *   예) https://kauth.kakao.com/.well-known/jwks.json
	 */
	private String jwkSetUri;
}
