package com.oneco.backend.global.security.jwt;

import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.JwtErrorCode;
import com.oneco.backend.global.security.jwt.config.JwtProperties;
import com.oneco.backend.global.security.jwt.config.JwtPurpose;

import io.jsonwebtoken.JwsHeader;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.RequiredArgsConstructor;

/**
 * JwtTokenValidator
 *
 * 역할
 * - 토큰이 유효한지 검증하고,
 *   검증이 완료된 Claims(페이로드)를 안전하게 반환한다.
 *
 * 설계 포인트
 * 1. 토큰 목적 (ACCESS/REFRESH/ONBOARDING)을 검증한다.
 *
 * 2. Header의 alg(서명 알고리즘)도 정책과 일치하는지 검증한다.
 *
 * 3. 예외(BaseException + JwtErrorCode)
 * 	  - 만료: EXPIRED_TOKEN
 * 	  - 그 외 서명/형식/파싱 오류: INVALID_TOKEN
 * 	  - 토큰 누락/공백: TOKEN_NOT_FOUND
 * 	  - 알고리즘 mismatch: TOKEN_ALG_MISMATCH
 * 	  - 목적 mistmatch : TOKEN_PURPOSE_MISMATCH
 */
@RequiredArgsConstructor
@Component
public class JwtTokenValidator {
	private static final String CLAIM_PURPOSE = "purpose";

	private final JwtProperties jwtProperties;
	private final JwtKeyProvider jwtKeyProvider;
	public Claims validateAndGetClaims(String token, JwtPurpose expectedPurpose){
		// token이 null이 아니고 빈 문자열/공백 문자열이 아닌지 확인
		requireText(token);
		// 객체가 null이 아닌지 확인
		Objects.requireNonNull(expectedPurpose, "expectedPurpose must not be null");

		SecretKey key= jwtKeyProvider.getKey(expectedPurpose);
		JwtProperties.PurposeProps props = jwtProperties.get(expectedPurpose);
		MacAlgorithm expectedAlg = props.getAlgorithm().toJjwt();

		try{
			// JJWT 파서 구성
			Jws<Claims> jws = Jwts.parser()
				.verifyWith(key) // 서명 검증용 키 지정
				.build()
				.parseSignedClaims(token); //서명된 JWT를 파싱하고 Claims까지 반환

			// payload(Claims) 추출
			Claims claims = jws.getPayload();

			// purpose 클레임 검증
			String claimPurpose = claims.get(CLAIM_PURPOSE, String.class);
			if (claimPurpose == null) {
				throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
			}
			if(!expectedPurpose.name().equals(claimPurpose)) {
				throw BaseException.from(JwtErrorCode.TOKEN_PURPOSE_MISMATCH);
			}
			// header alg 검증
			validateHeaderAlgorithm(jws.getHeader(), expectedAlg);

			return claims;

		}catch(ExpiredJwtException e){
			// 만료는 정상적인 인증 실패 케이스
			throw BaseException.from(JwtErrorCode.EXPIRED_TOKEN);
		}catch(JwtException | IllegalArgumentException e){
			// 서명/형식/파싱 오류
			throw BaseException.from(JwtErrorCode.INVALID_TOKEN);
		}

	}

	private void validateHeaderAlgorithm(JwsHeader header, MacAlgorithm expectedAlg){
		if(header==null){
			throw BaseException.from(JwtErrorCode.TOKEN_ALG_MISMATCH);
		}

		// JJWT 헤더의 알고리즘 문자열 (예: "HS256")
		String actual = header.getAlgorithm();

		//MacAlgorithm의 id
		String expected = expectedAlg.getId();

		if(actual == null || !expected.equals(actual)){
			throw BaseException.from(JwtErrorCode.TOKEN_ALG_MISMATCH);
		}
	}
	private void requireText(String token){
		if(token == null || token.isBlank()){
			throw BaseException.from(JwtErrorCode.TOKEN_NOT_FOUND);
		}
	}
}
