package com.oneco.backend.auth.application.bridge;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.oneco.backend.auth.application.dto.KakaoLoginResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisLoginBridgeStore implements LoginBridgeStore {

	private static final Duration TTL = Duration.ofSeconds(60);
	private static final String PREFIX = "login-bridge:";

	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	public String save(KakaoLoginResponse response) {
		String key = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set(PREFIX + key, response, TTL);
		return key;
	}

	@Override
	public KakaoLoginResponse consume(String key) {
		String redisKey = PREFIX + key;

		Object value = redisTemplate.opsForValue().get(redisKey);
		redisTemplate.delete(redisKey); // ✅ 1회성

		if (value == null) {
			return null;
		}
		return (KakaoLoginResponse) value;
	}
}
