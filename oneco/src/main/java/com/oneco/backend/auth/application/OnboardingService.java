package com.oneco.backend.auth.application;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.oneco.backend.auth.application.dto.OnboardingRequest;
import com.oneco.backend.auth.application.dto.TokensResponse;
import com.oneco.backend.auth.domain.oauth.SocialAccount;
import com.oneco.backend.auth.domain.oauth.SocialAccountRepository;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.exception.constant.MemberErrorCode;
import com.oneco.backend.global.security.jwt.JwtClaimExtractor;
import com.oneco.backend.global.security.jwt.JwtTokenProvider;
import com.oneco.backend.member.application.MemberService;
import com.oneco.backend.member.domain.Member;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OnboardingService {
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberService memberService;
	private final JwtClaimExtractor jwtClaimExtractor;
	private final SocialAccountRepository socialAccountRepository;
	// todo: Redis 붙이기

	@Transactional
	public TokensResponse complete(Claims claims, OnboardingRequest request) {
		Objects.requireNonNull(claims, "claims must not be null");
		Objects.requireNonNull(request, "request must not be null");
		log.info("OnboardingService.complete called with claims: {}, request: {}", claims, request);

		SocialSubject socialSubject = jwtClaimExtractor.getSocialSubject(claims);
		log.info("Extracted socialSubject: {}", socialSubject);

		SocialAccount socialAccount = socialAccountRepository.findByProviderAndSocialAccountId(
			socialSubject.provider(),
			socialSubject.socialAccountId()
		).orElseThrow(() -> BaseException.from(MemberErrorCode.SOCIAL_ACCOUNT_NOT_FOUND));

		Member member = socialAccount.getMember();
		Long memberId = member.getId();

		// 온보딩 완료 가능 상태인지 점검
		if (!memberService.canCompleteOnboarding(member.getStatus())) {
			throw BaseException.from(MemberErrorCode.ONBOARDING_NOT_ALLOWED);
		}

		/**
		 * 여기서 실제 추가정보를 반영
		 */
		if (request.familyRole() == null) {
			throw BaseException.from(MemberErrorCode.INVALID_ONBOARDING_DATA);
		}
		member.completeOnboarding(request.familyRole());

		String access = jwtTokenProvider.createAccessToken(memberId, "ROLE_USER");
		// redis 관리 정책 세우기
		String refresh = jwtTokenProvider.createRefreshToken(memberId);

		return new TokensResponse(access, refresh);
	}
}
