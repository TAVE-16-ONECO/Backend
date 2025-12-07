package com.oneco.backend.auth.application.dto;

public record KakaoLoginResponse(
	boolean isNew,
	String accessToken,
	String refreshToken,
	String onboardingToken
){
	public static KakaoLoginResponse existing(String accessToken, String refreshToken){
		return new KakaoLoginResponse(false, accessToken, refreshToken, null);
	}
	public static KakaoLoginResponse onboarding(String onboardingToken){
		return new KakaoLoginResponse(true, null, null, onboardingToken);
	}



}

