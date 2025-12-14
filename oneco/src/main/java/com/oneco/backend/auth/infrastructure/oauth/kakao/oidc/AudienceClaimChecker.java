package com.oneco.backend.auth.infrastructure.oauth.kakao.oidc;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

import com.oneco.backend.auth.domain.oauth.KakaoErrorCode;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.ErrorCode;

/**
 * 역할
 * - aud 클레임 관련 실제 검증 규칙을 수행하는 내부 전용 컴포넌트
 * <p>
 * 설계 의도
 * - 이 클래스는 Spring Security의 결과-반환 방식에 얽매이지 않고
 * 우리 서비스의 예외 표준( BaseException + ErrorCode)을 그대로 사용한다.
 * <p>
 * SpringSecuirty가 단순히 boolean을 리턴하거나 예외를 던지는 방식이 아니라, 결과 객체(Result)를 반환하는 이유
 * 1. 여러 검증을 연쇄적으로 처리하기 위해서
 * - SpringSecurity는 토큰 하나를 검증할 때 여러 개의 Validator를 묶어서 실행한다.
 * - 예외를 던지면 프로그램 흐름이 거기서 끊기고 뒤에 있는 검증기들은 실행조차 못한다.
 * - 앞의 검증이 실패해 Result.failure()를 반환해도, 프레임워크가 이를 받아서 실패 목록에 담아두고 다음 검증을 계속 진행할 수 있다.
 * 2. 제어 흐름과 로직의 분리
 * - 검증기(Validator)는 검증만 해야지, 프로그램을 중단시킬지 말지를 결정하면 안 된다는 철학
 * - 검증기는 OAuth2TokenValidatorResult만 반환하고 이걸 받은 상위 객체 JwtDecoder가 행동을 결정한다.
 */
public class AudienceClaimChecker {
	// 기대하는 aud 값
	// 카카오에서는 REST API KEY( = client_id)
	private final String expectedAudience;

	// Validator에서 설정값을 Properties에서 직접 꺼내는 방식을 쓰지 않고 생성자로 넘기는 방식을 쓴다.
	// 이렇게 하면 Validator가 특정 Provider 설정에 강하게 결합되지 않고
	// aud 검증이라는 단일 책임을 가진 범용 컴포넌트로 유지된다.
	// 카카오 -> 카카오 REST API Key(=client_id)
	public AudienceClaimChecker(String expectedAudience) {
		// 방어적 프로그래밍 - expectedAudience는 서버 설정값이므로 null/blank면 서버 구성 문제
		if (expectedAudience == null || expectedAudience.isBlank()) {
			throw BaseException.from(KakaoErrorCode.OIDC_AUTH_FAILED);
		}
		this.expectedAudience = expectedAudience;
	}

	public void check(Jwt token) {
		//1. JWT에서 aud 클레임 목록을 꺼낸다.
		List<String> audience = token.getAudience();

		// 2. 보안 검증 - OIDC 표준상 aud는 필수값이다. 없으면 위조된 토큰일 가능성이 높다.
		if (audience == null || audience.isEmpty()) {
			throw BaseException.from(KakaoErrorCode.OIDC_AUTH_FAILED);
		}

		// 3. 보안 검증 - 토큰의 수신자(aud) 목록에 내 Client ID가 포함되어 있는지 확인한다.
		// contains를 쓰는 이유: 하나의 토큰이 여러 서비스를 위해 발급될 수도 있기 때문
		if (!audience.contains(expectedAudience)) {
			throw BaseException.from(KakaoErrorCode.OIDC_AUTH_FAILED);
		}

	}

}
