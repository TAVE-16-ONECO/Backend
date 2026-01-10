package com.oneco.backend.global.security.jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.oneco.backend.auth.domain.oauth.SocialProvider;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.JwtErrorCode;
import com.oneco.backend.global.security.jwt.config.JwtProperties;
import com.oneco.backend.global.security.jwt.config.JwtPurpose;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

	// key -> CLAIM_ROLE = "role"
	// value -> 메서드 파라미터 role ("ROLE_USER", "ROLE_ADMIN")
	private static final String CLAIM_ROLE = "role";

	// ONBOARDING, ACCESS, REFRESH
	private static final String CLAIM_PURPOSE = "purpose";
	private final JwtProperties jwtProperties;
	private final JwtKeyProvider jwtKeyProvider;

	public String createAccessToken(Long memberId, String role) {
		return createToken(
			JwtPurpose.ACCESS,
			String.valueOf(memberId),
			role,
			null
		);
	}

	public String createRefreshToken(Long memberId) {
		// refresh는 보통 role 같은 권한 클레임 최소화
		return createToken(
			JwtPurpose.REFRESH,
			String.valueOf(memberId),
			null,
			null
		);
	}

	public String createOnboardingToken(SocialProvider provider, String socialAccountId) {

		String onboardingId = provider.name() + ":" + socialAccountId;
		// todo: 온보딩 상태 서버 검증(Redis)
		// -> 만료시간(exp)는 언제까지 유효한가만 보장하고, 지금 이 사람이 정말 온보딩 중인가는 보장 못함
		// -> 온보딩 중간에 이탈하거나 완료되면 Redis 상태를 만료/삭제/완료 처리해 흐름을 제어하는 게 안전함(PENDING/COMPLETED 등)

		return createToken(
			JwtPurpose.ONBOARDING,
			onboardingId,
			null,
			null
		);
	}

	private String createToken(
		JwtPurpose purpose,
		String subject, //memberId or onboardingId
		String role,
		Map<String, Object> extraClaims
	) {
		long now = System.currentTimeMillis();
		JwtProperties.PurposeProps props = jwtProperties.get(purpose);
		// *1000 해서 ms 바꿔서 만료 시간 expiry 생성
		long validitySeconds = props.getValidityInSeconds();
		Date expiry = new Date(now + validitySeconds * 1000L);

		SecretKey key = jwtKeyProvider.getKey(purpose);
		MacAlgorithm alg = props.getAlgorithm().toJjwt();

		JwtBuilder builder = Jwts.builder()
			.subject(subject)
			.claim(CLAIM_PURPOSE, purpose.name())
			.issuedAt(new Date(now))
			.expiration(expiry);

		// Access 토큰 등 인가가 필요한 경우에만 role 클레임 포함
		if (role != null && !role.isBlank()) {
			builder.claim(CLAIM_ROLE, role);
		}

		// 필요 시 확장 클레임 추가
		if (extraClaims != null && !extraClaims.isEmpty()) {
			if (extraClaims.containsKey(CLAIM_PURPOSE) || extraClaims.containsKey(CLAIM_ROLE)) {
				throw new IllegalArgumentException("extraClaims에 이미 있는 claim을 넣을 수 없습니다.");
			}
			for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
				builder.claim(entry.getKey(), entry.getValue());
			}
		}

		//서명 및 직렬화
		// 결과: header.payload.signature
		return builder.signWith(key, alg).compact();
	}

	// Claims 기반 Authentication 생성
	// ACCESS 토큰일때만 접근 가능
	public Authentication getAuthentication(Claims claims) {
		// 객체가 null이면 안됨
		Objects.requireNonNull(claims, "claims must not be null");
		log.info("JwtTokenProvider.getAuthentication called with claims: {}", claims);
		/**
		 * 1) 사용자 식별자 확보
		 * - 최소한 subject는 있어야 인증된 사용자로 의미가 성립된다.
		 * - subject가 없으면 토큰 구조/발급 정책 위반으로 간주한다.
		 */
		String subject = claims.getSubject();
		if (subject == null || subject.isBlank()) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		/**
		 * 2) 목적 클레임이 ACCESS가 아니면 예외
		 */
		String purpose = claims.get(CLAIM_PURPOSE, String.class);
		if (!JwtPurpose.ACCESS.name().equals(purpose)) {
			throw BaseException.from(JwtErrorCode.TOKEN_PURPOSE_MISMATCH);
		}

		/**
		 * 3) sub = memberId이므로 String(subject) -> Long(memberId) 파싱
		 * 만약 sub을 memberId로 안두고 따로 둔다면 건너뛰기
		 */
		Long memberId;
		try {
			memberId = Long.parseLong(subject);
		} catch (NumberFormatException e) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		String roleClaim = claims.get(CLAIM_ROLE, String.class);
		String roleName = normalizeRoleName(roleClaim);
		JwtPrincipal principal = new JwtPrincipal(memberId, subject, "ACCESS", roleName);

		// 3) authorities 구성 (ROLE_PARENT)
		List<SimpleGrantedAuthority> authorities =
			Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));

		// Authentication 생성 (credentials는 보통 null 처리(JWT는 이미 서명으로 신뢰성 확보)
		// JWT는 무상태 기반 인증을 목표로 하는 경우가 많아 매 요청마다 UserDetailsService로 사용자 로딩을 강제할 필요가 없다.
		// JwtPrincipal은 토큰 검증 후 필요한 최소 정보만 담는 경량 모델이라 성능과 구조 단순성에 유리하다.
		//UserDetails가 더 적합한 경우는 언제인가?
		// - 사용자 정지/탈퇴/권한 변경 같은 상태를 매 요청에 강하게 반영해야 하거나
		//   계정 잠금/만료 정책을 Spring Security 표준 모델로 일관되게 운영하려는 경우 UserDetails 기반 설계가 더 자연스럽다.
		return new UsernamePasswordAuthenticationToken(principal, null, authorities);
	}



	/**
	 * "ROLE_PARENT" / "PARENT" 둘 다 들어와도 "PARENT"로 통일
	 */
	private String normalizeRoleName(String role) {
		if (role == null || role.isBlank()) return null;
		String r = role.trim();
		if (r.startsWith("ROLE_")) {
			r = r.substring("ROLE_".length());
		}
		return r.trim().toUpperCase(java.util.Locale.ROOT); // "PARENT" or "CHILD"
	}

}
