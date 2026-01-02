package com.oneco.backend.family.infrastructure.persistence;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.oneco.backend.family.application.port.out.InvitationCodeStorePort;
import com.oneco.backend.family.domain.invitation.dto.FamilyInvitationRedisInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvitationCodeRedisAdapter implements InvitationCodeStorePort {

	private static final String CODE_KEY_PREFIX = "family:invitation:code:";
	private static final String INVITER_KEY_PREFIX = "family:invitation:inviter:";

	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	public void save(String code, FamilyInvitationRedisInfo info, Duration ttl) {
		String codeKey = CODE_KEY_PREFIX + code;
		String inviterKey = INVITER_KEY_PREFIX + info.getInviterId();
		redisTemplate.opsForValue().set(codeKey, info, ttl); // 초대 코드 정보 저장 [ 코드 : info 매핑 ]
		redisTemplate.opsForValue().set(inviterKey, code, ttl); // inviterId로 초대 코드 매핑 저장 [ inviterId : 코드 매핑 ]
	}

	@Override
	public Optional<FamilyInvitationRedisInfo> find(String code) {
		String key = CODE_KEY_PREFIX + code;
		FamilyInvitationRedisInfo info = (FamilyInvitationRedisInfo)redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(info);
	}

	@Override
	public Optional<String> findCodeByInviterId(Long inviterId) {
		String key = INVITER_KEY_PREFIX + inviterId;
		String code = (String)redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(code);
	}

	@Override
	public Optional<Long> getRemainingSeconds(String code) {
		String key = CODE_KEY_PREFIX + code;
		Long seconds = redisTemplate.getExpire(key);
		if (seconds == null || seconds < 0) {
			return Optional.empty();
		}
		return Optional.of(seconds);
	}
}
