package com.oneco.backend.mission.application.port.out;

import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.member.domain.MemberId;

public interface FamilyRelationLookupPort {

	// 요청자와 수신자가 특정 FamilyRelation의 멤버인지 확인
	boolean isMembersOfRelation(FamilyRelationId relationId, MemberId requesterId, MemberId recipientId);

	// memberId로 FamilyRelationId 조회
	FamilyRelationId findRelationIdByMemberId(MemberId memberId);
}
