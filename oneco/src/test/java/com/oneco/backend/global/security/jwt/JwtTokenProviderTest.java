package com.oneco.backend.global.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.JwtErrorCode;
import com.oneco.backend.global.security.jwt.config.JwtProperties;
import com.oneco.backend.global.security.jwt.config.JwtPurpose;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class JwtTokenProviderTest {

	@Test
	void createAccessToken_embedsPurposeAndNormalizedRole() {
		Fixture fixture = createFixture();

		String token = fixture.provider().createAccessToken(10L, "USER");

		// JWT를 바로 파싱하여 클레임을 검증
		Claims claims = Jwts.parser()
			.verifyWith(fixture.keyProvider().getKey(JwtPurpose.ACCESS))
			.build()
			.parseSignedClaims(token)
			.getPayload();

		assertEquals("10", claims.getSubject());
		assertEquals(JwtPurpose.ACCESS.name(), claims.get("purpose"));
		assertEquals("ROLE_USER", claims.get("role"));
	}

	@Test
	void getAuthentication_buildsPrincipalAndAuthoritiesFromClaims() {
		Fixture fixture = createFixture();
		String token = fixture.provider().createAccessToken(99L, "ROLE_ADMIN");
		Claims claims = Jwts.parser()
			.verifyWith(fixture.keyProvider().getKey(JwtPurpose.ACCESS))
			.build()
			.parseSignedClaims(token)
			.getPayload();

		Authentication authentication = fixture.provider().getAuthentication(claims);

		assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
		JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
		assertEquals(99L, principal.memberId());
		assertEquals("ACCESS", principal.purpose());
		assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}

	@Test
	void getAuthentication_whenSubjectMissing_throwsInvalidToken() {
		Fixture fixture = createFixture();
		Claims claims = Jwts.claims();
		claims.put("purpose", JwtPurpose.ACCESS.name());

		BaseException ex = assertThrows(BaseException.class, () -> fixture.provider().getAuthentication(claims));
		assertEquals(JwtErrorCode.INVALID_TOKEN.getCode(), ex.getCode());
	}

	@Test
	void getAuthentication_whenPurposeNotAccess_throwsPurposeMismatch() {
		Fixture fixture = createFixture();
		Claims claims = Jwts.claims();
		claims.setSubject("1");
		claims.put("purpose", JwtPurpose.REFRESH.name());

		BaseException ex = assertThrows(BaseException.class, () -> fixture.provider().getAuthentication(claims));
		assertEquals(JwtErrorCode.TOKEN_PURPOSE_MISMATCH.getCode(), ex.getCode());
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
		properties.getOnboarding().setValidityInSeconds(300);

		JwtKeyProvider keyProvider = new JwtKeyProvider(properties);
		keyProvider.init(); // @PostConstruct 대체 호출

		JwtTokenProvider provider = new JwtTokenProvider(properties, keyProvider);
		return new Fixture(properties, keyProvider, provider);
	}

	private record Fixture(JwtProperties properties, JwtKeyProvider keyProvider, JwtTokenProvider provider) {
	}
}
