package com.oneco.backend.global.security.jwt.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.oneco.backend.global.exception.JwtAuthenticationException;
import com.oneco.backend.global.security.jwt.BearerTokenExtractor;
import com.oneco.backend.global.security.jwt.JwtAuthenticationEntryPoint;
import com.oneco.backend.global.security.jwt.JwtTokenValidator;
import com.oneco.backend.global.security.jwt.config.JwtPurpose;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OnboardingTokenFilter extends OncePerRequestFilter {

	public static final String ATTR_ONBOARDING_CLAIMS = "ONBOARDING_CLAIMS";

	private final JwtTokenValidator jwtTokenValidator;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request){
		// 온보딩 토큰이 필요한 특정 API에만 적용
		String uri = request.getRequestURI();
		return !uri.startsWith("/api/onboarding/complete");
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	)throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		try {
			// Bearer 토큰 추출
			String token = BearerTokenExtractor.extractOrThrow(header);
			log.info("Onboarding Token Filter 작동 - 토큰 추출 완료");

			// 온보딩 목적 검증 + claim 추출
			Claims claims = jwtTokenValidator.validateAndGetClaims(token, JwtPurpose.ONBOARDING);
			log.info("Onboarding Token Filter 작동 - 토큰 검증 및 클레임 추출 완료");

			// 컨트롤러에서 쓰기 쉽게 request attribute로 전달
			// attribute map은 이 요청이 끝나면 사라짐, 클라이언트는 이 값을 절대 볼 수 없음
			// 서버가 같은 요청 처리 과정 안에서만 공유하는 메모리 슬롯
			request.setAttribute(ATTR_ONBOARDING_CLAIMS, claims);
			log.info("Onboarding Token Filter 작동 - request attribute에 클레임 저장 완료");

			filterChain.doFilter(request, response);
		}catch(JwtAuthenticationException e){
			SecurityContextHolder.clearContext();
			jwtAuthenticationEntryPoint.commence(request, response, e);
			return;
		}
	}
}
