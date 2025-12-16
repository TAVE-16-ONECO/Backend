package com.oneco.backend.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oneco.backend.auth.application.AuthService;
import com.oneco.backend.auth.application.KakaoOidcService;
import com.oneco.backend.auth.application.dto.KakaoLoginResponse;
import com.oneco.backend.auth.domain.oauth.SocialAccount;
import com.oneco.backend.auth.domain.oauth.SocialAccountRepository;
import com.oneco.backend.auth.domain.oauth.SocialProvider;
import com.oneco.backend.auth.infrastructure.oauth.kakao.client.KakaoOAuthClient;
import com.oneco.backend.auth.infrastructure.oauth.kakao.dto.KakaoTokenResponse;
import com.oneco.backend.auth.infrastructure.oauth.kakao.oidc.KakaoOidcClaims;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.GlobalErrorCode;
import com.oneco.backend.global.security.jwt.JwtTokenProvider;
import com.oneco.backend.member.domain.Member;
import com.oneco.backend.member.domain.MemberRepository;
import com.oneco.backend.member.domain.MemberStatus;
import com.oneco.backend.member.domain.SystemRole;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private KakaoOAuthClient kakaoOAuthClient;
	@Mock
	private KakaoOidcService kakaoOidcService;
	@Mock
	private SocialAccountRepository socialAccountRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private AuthService authService;

	@Test
	void loginWithKakao_existingActiveMember_returnsAccessAndRefreshTokens() throws Exception {
		// given: kakao에서 내려준 토큰과 파싱된 클레임
		String kakaoSub = "kakao-123";
		when(kakaoOAuthClient.requestAccessToken("auth-code"))
			.thenReturn(new KakaoTokenResponse("ka", "kr", 10L, "Bearer", "id-token", "scope"));
		when(kakaoOidcService.verifyAndParse("id-token"))
			.thenReturn(new KakaoOidcClaims(kakaoSub, "nickname", "profile", Instant.now(), Instant.now()));

		// given: 이미 가입 완료된 회원이 존재
		Member member = Member.createForOnboarding("profile", "nickname", SystemRole.USER);
		member.changeStatus(MemberStatus.ACTIVE);
		setId(member, 1L);
		SocialAccount socialAccount = SocialAccount.create(member, SocialProvider.KAKAO, kakaoSub);
		when(socialAccountRepository.findByProviderAndSocialAccountId(SocialProvider.KAKAO, kakaoSub))
			.thenReturn(Optional.of(socialAccount));

		// given: 액세스/리프레시 토큰 생성 결과
		when(jwtTokenProvider.createAccessToken(1L, "ROLE_USER")).thenReturn("access-token");
		when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");

		// when: 정상적인 state 값으로 로그인 처리
		KakaoLoginResponse response = authService.loginWithKakao("auth-code", "expected", "expected");

		// then: 기존 회원 흐름이므로 isNew=false이며 access/refresh가 채워진다.
		assertFalse(response.isNew());
		assertEquals("access-token", response.accessToken());
		assertEquals("refresh-token", response.refreshToken());
		assertNull(response.onboardingToken());
	}

	@Test
	void loginWithKakao_existingOnboardingMember_returnsOnboardingTokenOnly() throws Exception {
		String kakaoSub = "kakao-onboarding";
		when(kakaoOAuthClient.requestAccessToken("auth-code"))
			.thenReturn(new KakaoTokenResponse("ka", "kr", 10L, "Bearer", "id-token", "scope"));
		when(kakaoOidcService.verifyAndParse("id-token"))
			.thenReturn(new KakaoOidcClaims(kakaoSub, "nickname", "profile", Instant.now(), Instant.now()));

		// given: 온보딩 상태의 회원이면 기존 소셜 계정이 있어도 온보딩 토큰만 발급한다.
		Member member = Member.createForOnboarding("profile", "nickname", SystemRole.USER);
		setId(member, 2L);
		SocialAccount socialAccount = SocialAccount.create(member, SocialProvider.KAKAO, kakaoSub);
		when(socialAccountRepository.findByProviderAndSocialAccountId(SocialProvider.KAKAO, kakaoSub))
			.thenReturn(Optional.of(socialAccount));

		when(jwtTokenProvider.createOnboardingToken(SocialProvider.KAKAO, kakaoSub))
			.thenReturn("onboarding-token");

		KakaoLoginResponse response = authService.loginWithKakao("auth-code", "s", "s");

		// then: 온보딩 분기이므로 isNew=true, 온보딩 토큰만 존재한다.
		assertTrue(response.isNew());
		assertEquals("onboarding-token", response.onboardingToken());
		assertNull(response.accessToken());
		assertNull(response.refreshToken());
	}

	@Test
	void loginWithKakao_newMember_persistsSkeletonMemberAndReturnsOnboardingToken() throws Exception {
		String kakaoSub = "newbie-1";
		when(kakaoOAuthClient.requestAccessToken("code"))
			.thenReturn(new KakaoTokenResponse("ka", "kr", 10L, "Bearer", "id-token", "scope"));
		when(kakaoOidcService.verifyAndParse("id-token"))
			.thenReturn(new KakaoOidcClaims(kakaoSub, "newbie", "profile", Instant.now(), Instant.now()));

		// given: 소셜 계정이 없으므로 신규 온보딩 흐름
		when(socialAccountRepository.findByProviderAndSocialAccountId(SocialProvider.KAKAO, kakaoSub))
			.thenReturn(Optional.empty());
		when(jwtTokenProvider.createOnboardingToken(SocialProvider.KAKAO, kakaoSub))
			.thenReturn("onboarding-token");

		// memberRepository.save(...) 호출 시 id를 세팅해주는 답변을 등록
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
			Member saved = invocation.getArgument(0);
			setId(saved, 99L);
			return saved;
		});
		when(socialAccountRepository.save(any(SocialAccount.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		KakaoLoginResponse response = authService.loginWithKakao("code", "state", "state");

		// then: 새 회원이므로 온보딩 토큰이 발급되며, 생성된 회원은 ONBOARDING 상태를 유지한다.
		assertTrue(response.isNew());
		assertEquals("onboarding-token", response.onboardingToken());

		// verify: member/social 저장 시점에 받은 엔티티도 확인
		ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(memberCaptor.capture());
		assertEquals(MemberStatus.ONBOARDING, memberCaptor.getValue().getStatus());

		ArgumentCaptor<SocialAccount> socialCaptor = ArgumentCaptor.forClass(SocialAccount.class);
		verify(socialAccountRepository).save(socialCaptor.capture());
		assertEquals(SocialProvider.KAKAO, socialCaptor.getValue().getProvider());
		assertEquals(kakaoSub, socialCaptor.getValue().getSocialAccountId());
	}

	@Test
	void loginWithKakao_invalidState_throwsBaseException() {
		// when & then: state 값이 다르면 INVALID_OAUTH_STATE 예외가 발생
		BaseException ex = assertThrows(BaseException.class,
			() -> authService.loginWithKakao("code", "actual", "expected"));
		assertEquals(GlobalErrorCode.INVALID_OAUTH_STATE.getCode(), ex.getCode());
	}

	private void setId(Member member, long id) throws Exception {
		Field field = Member.class.getDeclaredField("id");
		field.setAccessible(true);
		field.set(member, id);
	}
}
