package com.oneco.backend.mission.application.port.out;

import java.util.Optional;

import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.member.domain.MemberId;

public interface FamilyRelationLookupPort {

	// 요청자와 수신자가 특정 FamilyRelation의 멤버인지 확인
	boolean isMembersOfRelation(FamilyRelationId relationId, MemberId requesterId, MemberId recipientId);

	// memberId로 FamilyRelationId 조회
	FamilyRelationId findRelationIdByMemberId(MemberId memberId);

	// 요청자와 수신자의 연결된 가족 관계 ID 조회
	Optional<FamilyRelationId> findConnectedRelationIdBetween(MemberId requesterId, MemberId recipientId);
}
