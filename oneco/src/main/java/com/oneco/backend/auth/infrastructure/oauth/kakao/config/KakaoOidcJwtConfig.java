package com.oneco.backend.auth.infrastructure.oauth.kakao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.oneco.backend.auth.infrastructure.oauth.kakao.oidc.AudienceValidator;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KakaoOidcJwtConfig {

	private final KakaoOIDCProperties kakaoOIDCProperties;
	private final KakaoOAuthProperties kakaoOAuthProperties;

	@Bean
	public JwtDecoder  kakaoIdTokenDecoder(){

		/**
		 * 1) NimbusJwtDecoder 생성
		 *
		 * - NimbusJwtDecoder는 Spring Security가 감싼 Nimbus JOSE + JWT 기반
		 * - withJwkSetUri(...)는
		 *   "카카오가 제공하는 공개키 목록(JWKS)"에서 키를 가져와 id_token의 서명을 검증할 수 있게 구성한다.
		 *
		 *   이 단계 자체는 서명 검증에 필요한 재료(공개키 소스)를 준비하는 과정
		 *
		 *   실제 서명 검증은 나중에 decode(idToken) 호출 시 수행된다.
		 */
		NimbusJwtDecoder decoder = NimbusJwtDecoder
			.withJwkSetUri(kakaoOIDCProperties.getJwkSetUri())
			.build();

		/**
		 * 2) 기본 검증 + issuer 검증 Validator 생성
		 * JwtValidators.createDefaultWithIssuer(...)는 Spring Security가 이미 만들어서 제공하는 기본 Validator 묶음
		 *   - exp(만료 시간) 검증
		 *   - nbf(사용 가능 시각) 검증
		 *   - iss(발급자) 검증
		 * 즉, 시간적으로 유효하고, 발급자가 카카오가 맞는지를 검증한다.
		 */
		OAuth2TokenValidator<Jwt> withIssuer=
			JwtValidators.createDefaultWithIssuer(kakaoOIDCProperties.getIssuer());

		/**
		 * 3) aud 검증 Vaidator 생성
		 * - aud는 이 토큰이 어떤 클라이언트(앱)를 위해 발급되었는가를 의미
		 * - 카카오 OIDC id_token에서는 aud에 clientId가 포함된다.
		 */
		OAuth2TokenValidator<Jwt> withAudience =
			new AudienceValidator(kakaoOAuthProperties.getClientId());

		/**
		 * 4) Validator 체인 결합
		 *
		 * DelegationOAuth2TokenValidator은 여러 Validator를 순서대로 실행하는 합성 Validator
		 * - decode(idToken) 내부에서 JWT 파싱/서명 검증이 끝난 뒤
		 * - withIssuer -> withAudience 순으로 검증이 진행된다.
		 * - 하나라도 실패하면 전체 실패 처리
		 */
		OAuth2TokenValidator<Jwt> validator =
			 new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);

		/**
		 * 5) decoder에 validator 연결
		 * 이 줄이 있어야 decode(idToken) 시점에 위 검증들이 실제로 실행된다.
		 */
		decoder.setJwtValidator(validator);

		/**
		 * 6) 최종적으로 카카오 id_token 전용 Decder 반환
		 * 이 반환된 빈은 JwtAuthenticationFilter에 자동으로 붙지 않는다.
		 * AuthService / Verifier가 필요할 때 직접 주입 받아 사용한다.
		 */
		return decoder;
	}
}
