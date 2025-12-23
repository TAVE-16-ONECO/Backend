package com.oneco.backend.family.infrastructure.redis;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.oneco.backend.family.application.port.out.InvitationCodeGenerator;

@Component
public class RandomInvitationCodeGenerator implements InvitationCodeGenerator {

	private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 헷갈리는 문자 제거
	private static final int LENGTH = 8;

	private final SecureRandom random = new SecureRandom();

	@Override
	public String generate() {
		StringBuilder sb = new StringBuilder(LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
		}
		return sb.toString();
	}
}
