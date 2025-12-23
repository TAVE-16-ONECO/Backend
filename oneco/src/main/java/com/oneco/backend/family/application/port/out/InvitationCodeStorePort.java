package com.oneco.backend.family.application.port.out;

import java.time.Duration;
import java.util.Optional;

import com.oneco.backend.family.domain.invitation.dto.FamilyInvitationRedisInfo;

public interface InvitationCodeStorePort {
	// 초대 코드와 초대 정보 저장
	void save(String code, FamilyInvitationRedisInfo info, Duration ttl);

	// 초대 코드로 초대 정보 조회
	Optional<FamilyInvitationRedisInfo> find(String code);

	// inviterId로 초대 코드 조회
	Optional<String> findCodeByInviterId(Long inviterId);

	// 초대 코드의 남은 유효 시간(초) 조회
	Optional<Long> getRemainingSeconds(String code);
}
