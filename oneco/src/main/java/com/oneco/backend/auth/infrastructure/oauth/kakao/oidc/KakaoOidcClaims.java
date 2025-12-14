package com.oneco.backend.auth.infrastructure.oauth.kakao.oidc;

import java.time.Instant;

import org.springframework.security.oauth2.jwt.Jwt;

public record KakaoOidcClaims(
	String sub,
	//	String email,
	String nickname,
	String profileImageUrl,
	Instant issuedAt,
	Instant expiresAt
) {
	public static KakaoOidcClaims from(Jwt token) {
		String sub = token.getSubject();
		//String email = token.getClaimAsString("email");
		String nickname = token.getClaimAsString("nickname");
		Instant issuedAt = token.getIssuedAt();
		Instant expiresAt = token.getExpiresAt();
		String profileImgUrl = token.getClaimAsString("picture");
		return new KakaoOidcClaims(
			sub,
			//	email,
			nickname,
			profileImgUrl,
			token.getIssuedAt(),
			token.getExpiresAt()
		);
	}
}
