package com.oneco.backend.global.security.jwt.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtPurpose {
	ACCESS,
	REFRESH,
	ONBOARDING
}
