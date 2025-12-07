package com.oneco.backend.global.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.oneco.backend.auth.application.SocialSubject;
import com.oneco.backend.auth.domain.oauth.SocialProvider;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.JwtErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class JwtClaimExtractorTest {

	private final JwtClaimExtractor extractor = new JwtClaimExtractor();

	@Test
	void getSocialSubject_parsesProviderAndId() {
		Claims claims = Jwts.claims();
		claims.setSubject("KAKAO:12345");

		SocialSubject subject = extractor.getSocialSubject(claims);

		assertEquals(SocialProvider.KAKAO, subject.provider());
		assertEquals("12345", subject.socialAccountId());
	}

	@Test
	void getSocialSubject_whenFormatInvalid_throwsInvalidToken() {
		assertInvalidSubject(null);
		assertInvalidSubject("");
		assertInvalidSubject("NOCOLON");
		assertInvalidSubject("UNKNOWN:abc");
		assertInvalidSubject("KAKAO:");
	}

	private void assertInvalidSubject(String subjectValue) {
		Claims claims = Jwts.claims();
		if (subjectValue != null) {
			claims.setSubject(subjectValue);
		} else {
			claims.setSubject(null);
		}

		BaseException ex = assertThrows(BaseException.class, () -> extractor.getSocialSubject(claims));
		assertEquals(JwtErrorCode.INVALID_TOKEN.getCode(), ex.getCode());
	}
}
