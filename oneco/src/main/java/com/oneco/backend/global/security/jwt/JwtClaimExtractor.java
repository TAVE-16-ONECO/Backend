package com.oneco.backend.global.security.jwt;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;

import com.oneco.backend.auth.application.SocialSubject;
import com.oneco.backend.auth.domain.oauth.SocialProvider;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.JwtErrorCode;

/**
 * JwtClaimExtractor
 * <p>
 * 역할
 * - Claims에서 프로젝트 표준 정보 추출 책임을 전담한다.
 * <p>
 * 장점
 * - sub 정책이 바뀌어도 이 컴포넌트만 수정하면 된다.
 * - 테스트가 쉽고, 의존성 주입 흐름이 자연스럽다.
 */
@Component
public class JwtClaimExtractor {

	public SocialSubject getSocialSubject(Claims claims) {
		if (claims == null) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		String subject = claims.getSubject();
		if (subject == null || subject.isBlank()) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		// 기대 포맷: "KAKAO:123", "NAVER:abc", "GOOGLE:xyz"
		String[] parts = subject.split(":", 2);
		if (parts.length != 2) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		String providerRaw = parts[0];
		String socialAccountId = parts[1];

		if (providerRaw.isBlank() || socialAccountId.isBlank()) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		SocialProvider provider;
		try {
			provider = SocialProvider.valueOf(providerRaw);
		} catch (IllegalArgumentException e) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		return new SocialSubject(provider, socialAccountId);
	}

}

