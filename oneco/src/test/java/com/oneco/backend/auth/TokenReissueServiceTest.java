package com.oneco.backend.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oneco.backend.auth.application.TokenReissueService;
import com.oneco.backend.auth.application.dto.TokenReissueResponse;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.JwtErrorCode;
import com.oneco.backend.global.security.jwt.JwtTokenProvider;
import com.oneco.backend.global.security.jwt.filter.RefreshTokenFilter;
import com.oneco.backend.global.security.jwt.config.JwtPurpose;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class TokenReissueServiceTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private RefreshTokenFilter refreshTokenFilter;

	@InjectMocks
	private TokenReissueService tokenReissueService;

	@Test
	void reissue_withValidRefreshClaims_issuesNewAccessToken() {
		// given: refresh 토큰의 claims가 정상적으로 주입된 상황
		Claims claims = Jwts.claims();
		claims.setSubject("42");
		when(jwtTokenProvider.createAccessToken(42L, JwtPurpose.ACCESS.name()))
			.thenReturn("new-access-token");

		// when
		TokenReissueResponse response = tokenReissueService.reissue(claims);

		// then: 숫자 subject가 정상 파싱되어 새 액세스 토큰이 발급된다.
		assertEquals("new-access-token", response.accessToken());
	}

	@Test
	void reissue_whenSubjectMissing_throwsInvalidToken() {
		Claims claims = Jwts.claims(); // subject가 비어있는 상태

		BaseException ex = assertThrows(BaseException.class, () -> tokenReissueService.reissue(claims));
		assertEquals(JwtErrorCode.INVALID_TOKEN.getCode(), ex.getCode());
	}

	@Test
	void reissue_whenSubjectNotNumber_throwsInvalidToken() {
		Claims claims = Jwts.claims();
		claims.setSubject("not-a-number");

		BaseException ex = assertThrows(BaseException.class, () -> tokenReissueService.reissue(claims));
		assertEquals(JwtErrorCode.INVALID_TOKEN.getCode(), ex.getCode());
	}
}
