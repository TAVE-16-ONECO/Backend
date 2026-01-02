package com.oneco.backend.auth.application;

import com.oneco.backend.auth.domain.oauth.SocialProvider;

public record SocialSubject(
	SocialProvider provider,
	String socialAccountId

) {
}
