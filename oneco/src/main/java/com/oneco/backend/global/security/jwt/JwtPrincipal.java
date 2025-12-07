package com.oneco.backend.global.security.jwt;

public record JwtPrincipal(
	Long memberId,
	String subject,
	String purpose
) {
}
