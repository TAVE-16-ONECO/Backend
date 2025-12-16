package com.oneco.backend.global.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.oneco.backend.auth.domain.oauth.SocialProvider;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.JwtErrorCode;
import com.oneco.backend.global.security.jwt.config.JwtMacAlgorithm;
import com.oneco.backend.global.security.jwt.config.JwtProperties;
import com.oneco.backend.global.security.jwt.config.JwtPurpose;

import io.jsonwebtoken.Claims;

class JwtTokenValidatorTest {

	@Test
	void validateAndGetClaims_returnsClaimsWhenTokenValid() {
		Fixture fixture = createFixture();
		String token = fixture.provider().createAccessToken(15L, "ROLE_USER");

		Claims claims = fixture.validator().validateAndGetClaims(token, JwtPurpose.ACCESS);

		assertEquals("15", claims.getSubject());
		assertEquals(JwtPurpose.ACCESS.name(), claims.get("purpose"));
	}

	@Test
	void validateAndGetClaims_whenPurposeMismatch_throws() {
		Fixture fixture = createFixture();
		String onboardingToken = fixture.provider().createOnboardingToken(SocialProvider.KAKAO, "sub-1");

		BaseException ex = assertThrows(BaseException.class,
			() -> fixture.validator().validateAndGetClaims(onboardingToken, JwtPurpose.ACCESS));
		assertEquals(JwtErrorCode.TOKEN_PURPOSE_MISMATCH.getCode(), ex.getCode());
	}

	@Test
	void validateAndGetClaims_whenTokenMissing_throwsTokenNotFound() {
		Fixture fixture = createFixture();

		BaseException ex = assertThrows(BaseException.class,
			() -> fixture.validator().validateAndGetClaims(" ", JwtPurpose.ACCESS));
		assertEquals(JwtErrorCode.TOKEN_NOT_FOUND.getCode(), ex.getCode());
	}

	@Test
	void validateAndGetClaims_whenAlgorithmUnexpected_throwsAlgMismatch() {
		Fixture fixture = createFixture();
		String token = fixture.provider().createAccessToken(1L, "ROLE_USER");

		// 기대 알고리즘을 HS512로 바꿔 header.alg와 의도적으로 다르게 만든다.
		fixture.properties().getAccess().setAlgorithm(JwtMacAlgorithm.HS512);

		BaseException ex = assertThrows(BaseException.class,
			() -> fixture.validator().validateAndGetClaims(token, JwtPurpose.ACCESS));
		assertEquals(JwtErrorCode.TOKEN_ALG_MISMATCH.getCode(), ex.getCode());
	}

	private Fixture createFixture() {
		JwtProperties properties = new JwtProperties();
		String baseSecret = Base64.getEncoder()
			.encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

		properties.getAccess().setSecretKey(baseSecret);
		properties.getAccess().setValidityInSeconds(3600);

		properties.getRefresh().setSecretKey(baseSecret);
		properties.getRefresh().setValidityInSeconds(3600);

		properties.getOnboarding().setSecretKey(baseSecret);
		properties.getOnboarding().setValidityInSeconds(600);

		JwtKeyProvider keyProvider = new JwtKeyProvider(properties);
		keyProvider.init();

		JwtTokenProvider provider = new JwtTokenProvider(properties, keyProvider);
		JwtTokenValidator validator = new JwtTokenValidator(properties, keyProvider);
		return new Fixture(properties, keyProvider, provider, validator);
	}

	private record Fixture(
		JwtProperties properties,
		JwtKeyProvider keyProvider,
		JwtTokenProvider provider,
		JwtTokenValidator validator
	) {
	}
}
