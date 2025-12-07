package com.oneco.backend.auth.application;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.oneco.backend.auth.domain.oauth.KakaoErrorCode;
import com.oneco.backend.auth.infrastructure.oauth.kakao.oidc.KakaoOidcClaims;
import com.oneco.backend.global.exception.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOidcService {

	private final JwtDecoder kakaoIdTokenDecoder;

	/**
	 *
	 */
	public KakaoOidcClaims verifyAndParse(String idToken){
		try{
			if(idToken == null || idToken.isBlank()){
				throw BaseException.from(KakaoErrorCode.OIDC_AUTH_FAILED);
			}

			//1. JwtDecoder를 사용하여 서명/만료/iss/aud 검증 수행
			Jwt jwt = kakaoIdTokenDecoder.decode(idToken);

			//2. 검증이 끝난 Jwt 객체에서 필요한 클레임 추출
			KakaoOidcClaims claims = KakaoOidcClaims.from(jwt);

			log.debug("[KakaoOIDC] ID token verified. sub={}, nickname={}",
				claims.sub(), claims.nickname());

			return claims;
		}catch(JwtException exception){
			log.warn("[KakaoOIDC] Invalid ID token. message={}", exception.getMessage(), exception);
			throw BaseException.from(KakaoErrorCode.OIDC_AUTH_FAILED);
		}
	}
}
