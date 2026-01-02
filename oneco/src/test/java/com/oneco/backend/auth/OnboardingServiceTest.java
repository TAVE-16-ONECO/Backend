package com.oneco.backend.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oneco.backend.auth.application.OnboardingService;
import com.oneco.backend.auth.application.SocialSubject;
import com.oneco.backend.auth.application.dto.OnboardingRequest;
import com.oneco.backend.auth.application.dto.TokensResponse;
import com.oneco.backend.auth.domain.oauth.SocialAccount;
import com.oneco.backend.auth.domain.oauth.SocialAccountRepository;
import com.oneco.backend.auth.domain.oauth.SocialProvider;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.exception.constant.MemberErrorCode;
import com.oneco.backend.global.security.jwt.JwtClaimExtractor;
import com.oneco.backend.global.security.jwt.JwtTokenProvider;
import com.oneco.backend.member.application.MemberService;
import com.oneco.backend.member.domain.FamilyRole;
import com.oneco.backend.member.domain.Member;
import com.oneco.backend.member.domain.MemberStatus;
import com.oneco.backend.member.domain.SystemRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private MemberService memberService;
	@Mock
	private JwtClaimExtractor jwtClaimExtractor;
	@Mock
	private SocialAccountRepository socialAccountRepository;

	@InjectMocks
	private OnboardingService onboardingService;

	@Test
	void complete_whenOnboardingMember_updatesProfileAndIssuesTokens() throws Exception {
		// given: 온보딩 토큰에서 추출한 소셜 주체 정보
		Claims claims = Jwts.claims();
		SocialSubject subject = new SocialSubject(SocialProvider.KAKAO, "kakao-123");
		when(jwtClaimExtractor.getSocialSubject(claims)).thenReturn(subject);

		// given: 온보딩 상태의 회원과 연결된 소셜 계정
		Member member = Member.createForOnboarding("profile", "nick", SystemRole.USER);
		setId(member, 7L);
		SocialAccount socialAccount = SocialAccount.create(member, SocialProvider.KAKAO, "kakao-123");
		when(socialAccountRepository.findByProviderAndSocialAccountId(SocialProvider.KAKAO, "kakao-123"))
			.thenReturn(Optional.of(socialAccount));

		// given: 온보딩 완료 가능 여부와 토큰 생성 결과
		when(memberService.canCompleteOnboarding(MemberStatus.ONBOARDING)).thenReturn(true);
		when(jwtTokenProvider.createAccessToken(7L, "ROLE_USER")).thenReturn("new-access");
		when(jwtTokenProvider.createRefreshToken(7L)).thenReturn("new-refresh");

		OnboardingRequest request = new OnboardingRequest(FamilyRole.PARENT);

		// when: 온보딩 완료 처리
		TokensResponse tokens = onboardingService.complete(claims, request);

		// then: 회원 상태/역할이 업데이트되고 토큰이 발급된다.
		assertEquals("new-access", tokens.accessToken());
		assertEquals("new-refresh", tokens.refreshToken());
		assertEquals(MemberStatus.ACTIVE, member.getStatus());
		assertEquals(FamilyRole.PARENT, member.getFamilyRole());
	}

	@Test
	void complete_whenOnboardingNotAllowed_throwsConflict() throws Exception {
		Claims claims = Jwts.claims();
		SocialSubject subject = new SocialSubject(SocialProvider.KAKAO, "blocked");
		when(jwtClaimExtractor.getSocialSubject(claims)).thenReturn(subject);

		Member member = Member.createForOnboarding("p", "n", SystemRole.USER);
		member.changeStatus(MemberStatus.ACTIVE); // 이미 온보딩 완료된 상태
		setId(member, 3L);
		SocialAccount socialAccount = SocialAccount.create(member, SocialProvider.KAKAO, "blocked");
		when(socialAccountRepository.findByProviderAndSocialAccountId(SocialProvider.KAKAO, "blocked"))
			.thenReturn(Optional.of(socialAccount));

		when(memberService.canCompleteOnboarding(MemberStatus.ACTIVE)).thenReturn(false);

		// then: 허용되지 않는 상태에서는 예외로 흐름이 차단된다.
		BaseException ex = assertThrows(BaseException.class,
			() -> onboardingService.complete(claims, new OnboardingRequest(FamilyRole.CHILD)));
		assertEquals(MemberErrorCode.ONBOARDING_NOT_ALLOWED.getCode(), ex.getCode());
	}

	private void setId(Member member, long id) throws Exception {
		Field field = Member.class.getDeclaredField("id");
		field.setAccessible(true);
		field.set(member, id);
	}
}
