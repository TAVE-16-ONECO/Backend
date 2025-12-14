package com.oneco.backend.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final KakaoOAuthClient kakaoOAuthClient;
	private final KakaoOidcService kakaoOidcService;

	private final SocialAccountRepository socialAccountRepository;
	private final MemberRepository memberRepository;

	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public KakaoLoginResponse loginWithKakao(String code, String state, String expectedState) {
		// 1. state 검증
		validateState(state, expectedState);

		// 2. 인가코드 -> 토큰
		KakaoTokenResponse kakaoTokenResponse = kakaoOAuthClient.requestAccessToken(code);

		// 3. kakao idToken 토큰 검증 및 파싱
		KakaoOidcClaims claims = kakaoOidcService.verifyAndParse(kakaoTokenResponse.idToken());

		// 4. 카카오 고유 식별자 추출
		String kakaoSub = claims.sub();

		return socialAccountRepository
			.findByProviderAndSocialAccountId(SocialProvider.KAKAO, kakaoSub)
			.map(social -> {
				Member member = social.getMember();

				// ✅ 핵심: 소셜 계정이 있어도 온보딩 상태면 기존회원 처리 금지
				if (member.getStatus() == MemberStatus.ONBOARDING) {
					String onboardingToken =
						jwtTokenProvider.createOnboardingToken(SocialProvider.KAKAO, kakaoSub);

					return KakaoLoginResponse.onboarding(onboardingToken);
				}

				Long memberId = member.getId();
				String access = jwtTokenProvider.createAccessToken(memberId, "ROLE_USER");
				String refresh = jwtTokenProvider.createRefreshToken(memberId);
				return KakaoLoginResponse.existing(access, refresh);
			})
			.orElseGet(() -> {
				// 신규 회원 처리 (추천 패턴: 최소 레코드 선생성 + ONBOARDING 상태)
				// todo: 현재 온보딩 시작 시점에 Member 엔티티를 최소 필드로 먼저 생성
				//       이후 온보딩 완료 시점에 추가 정보 업데이트
				//       -> 추후 레디스를 이용한 임시 저장소 분리하기 (온보딩 완료 전에는 DB에 영구 저장하지 않는 방식)
				//       -> 지금은 간단히 Member 엔티티를 바로 생성하는 방식으로 구현
				//       -> MemberStatus.ONBOARDING 상태로 변경 (따로 ttl은 안둠)
				Member member = Member.createForOnboarding(claims.profileImageUrl(), claims.nickname(),
					SystemRole.USER); // 최소 필드만 채운 팩토리
				memberRepository.save(member);

				SocialAccount social = SocialAccount.create(
					member,
					SocialProvider.KAKAO,
					kakaoSub
				);
				socialAccountRepository.save(social);

				// 상태가 있다면 ONBOARDING 명시
				member.changeStatus(MemberStatus.ONBOARDING);

				String onboardingToken = jwtTokenProvider.createOnboardingToken(SocialProvider.KAKAO, kakaoSub);

				return KakaoLoginResponse.onboarding(onboardingToken);
			});
	}

	private void validateState(String state, String expectedState) {
		if (expectedState == null || !expectedState.equals(state)) {
			throw BaseException.from(GlobalErrorCode.INVALID_OAUTH_STATE);
		}
	}
}
