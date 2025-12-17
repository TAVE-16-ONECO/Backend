package com.oneco.backend.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.auth.application.dto.TokenReissueResponse;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.JwtErrorCode;
import com.oneco.backend.global.security.jwt.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

/**
 * TokenReissueService
 * <p>
 * 역할
 * - Refresh Claims 기반으로 새 Access Token 발급
 * <p>
 * 설계 의도
 * - Refresh 흐름은 "재발급 전용"
 * - SecurityContext에 Authentication을 굳이 세팅하지 않는다.
 */
@Service
@RequiredArgsConstructor
public class TokenReissueService {
	private final JwtTokenProvider jwtTokenProvider;
	// 만약 실제 회원 상태 검증이 필요하면 DB 확인
	// private final MemberRepository memberRepository;

	@Transactional(readOnly = true)
	public TokenReissueResponse reissue(Claims refreshClaims) {

		// sub에서 사용자 식별자(memberId) 추출
		String subject = refreshClaims.getSubject();
		if (subject == null || subject.isBlank()) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		Long memberId;

		// subject(String) -> memberId(Long) 파싱
		try {
			memberId = Long.parseLong(subject);
		} catch (NumberFormatException e) {
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

		// 만약 사용자 상태 검증할 시 사용( 탈퇴/정지/휴면 여부 확인 등)
		// Member member = memberRepository.findById(memberId)
		// 	.orElseThrow(()-> BaseException.from(MemberErrorCode.MEMBER_NOT_FOUND));

		// 새 Access Token 발급
		String newAccessToken = jwtTokenProvider.createAccessToken(memberId, "ROLE_USER");

		return new TokenReissueResponse(newAccessToken);
	}
}
