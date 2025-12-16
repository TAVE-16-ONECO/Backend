package com.oneco.backend.auth.infrastructure.oauth.kakao.oidc;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.oneco.backend.global.exception.BaseException;

/**
 * Kakao OIDC ID Token(JWT)의 aud(audience) 클레임을 검증하는 Validator
 * <p>
 * 왜 필요한가?
 * - aud는 이 토큰이 어떤 클라이언트(앱)를 대상으로 발급되었는지를 나타내는 클레임이다.
 * - 카카오 OIDC에서는 aud에 보통 앱의 REST API key(client_id)가 포함된다.
 * - 따라서 aud에 우리가 사용하는 clientId가 없으면
 * -> 다른 앱을 위해 발급된 토큰이거나
 * -> 공격자가 다른 앱 토큰을 들고 우리 서버를 시도하는 케이스일 수 있으므로
 * -> 인증을 실패시키는 것이 안전하다.
 * 적용 시점
 * - NimbusJwtDecoder가
 * 1) 서명 검증
 * 2) 만료(exp) 검증
 * 3) 기본 JWT 파싱
 * 을 완료한 후,
 * 추가 검증 단계에서 이 Validator가 실행된다.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

	private final AudienceClaimChecker checker;

	public AudienceValidator(String expectedAudience) {
		this.checker = new AudienceClaimChecker(expectedAudience);
	}

	/**
	 * JWT의 aud 클레임 검증
	 *
	 * @param token JwtDecoder가 파싱한 Jwt 객체
	 * @return 검증 성공/실패 결과
	 */
	@Override
	public OAuth2TokenValidatorResult validate(Jwt token) {
		try {
			// 실제 규칙 검증은 도메인 규칙 클래스인 Checker에게 위임한다.
			// 이 덕분에 validate()는 프레임워크 계약만 신경 쓰면 된다.
			checker.check(token);

			// 검증 통과
			return OAuth2TokenValidatorResult.success();
		} catch (BaseException e) {
			// Checker는 우리 표준대로 BaseException을 던진다.
			// 하지만 Spring Security Validator 레이어는 결과 반환 컨벤션을 따라야 하므로
			// 여기서 BaseException 예외를 잡아 OAuth2Error로 변환해 failure 결과로 반환한다.
			OAuth2Error error = new OAuth2Error(
				"invalid_token",
				"OIDC token validation failded",
				null
			);

			return OAuth2TokenValidatorResult.failure(error);
		}
	}
}
