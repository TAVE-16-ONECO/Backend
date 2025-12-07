package com.oneco.backend.auth.presentation;

import static com.oneco.backend.global.security.jwt.filter.OnboardingTokenFilter.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.auth.application.OnboardingService;
import com.oneco.backend.auth.application.dto.OnboardingRequest;
import com.oneco.backend.auth.application.dto.TokensResponse;
import com.oneco.backend.global.response.DataResponse;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {
	private final OnboardingService onboardingService;

	/**
	 * 온보딩 완료 엔드포인트.
	 *
	 * - 이 엔드포인트는 Access 인증 필터 대상이 아니다.
	 * - Onboarding 목적 토큰을 받아 목적/서명/만료를 검증한 뒤
	 *   회원을 최종 생성하고 Access/Refresh를 발급한다.
	 */
	@Operation(
		summary = "온보딩 완료",
		description = "Onboarding 토큰 목적/서명/만료 검증 후 회원 생성 및 Access/Refresh 발급"
	)
	@SecurityRequirement(name = "OnboardingToken")
	@PostMapping("/complete")
	public DataResponse<TokensResponse> complete(
		@Parameter(hidden = true)
		@RequestAttribute(ATTR_ONBOARDING_CLAIMS) Claims claims,
		@RequestBody OnboardingRequest request
	){
		TokensResponse response = onboardingService.complete(claims, request);
		return DataResponse.from(response);
	}
}
