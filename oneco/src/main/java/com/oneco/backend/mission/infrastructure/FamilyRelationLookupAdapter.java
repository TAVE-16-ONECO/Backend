package com.oneco.backend.mission.infrastructure;

import org.springframework.stereotype.Component;

import com.oneco.backend.family.domain.relation.FamilyRelation;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.family.domain.relation.RelationStatus;
import com.oneco.backend.family.infrastructure.persistence.FamilyRelationJpaRepository;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.family.domain.exception.constant.FamilyErrorCode;
import com.oneco.backend.mission.application.port.out.FamilyRelationLookupPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyRelationLookupAdapter implements FamilyRelationLookupPort {

	private final FamilyRelationJpaRepository repository;

	// 요청자와 수신자가 특정 FamilyRelation의 멤버인지 확인
	@Override
	public boolean isMembersOfRelation(FamilyRelationId relationId, MemberId requesterId, MemberId recipientId) {
		return repository.findById(relationId.getValue())
			.filter(relation -> relation.getStatus() == RelationStatus.CONNECTED) // 연결된 상태인지 확인
			.filter(relation -> isMembersMatched(relation, requesterId, recipientId)) // 멤버 일치 여부 확인
			.isPresent(); //
	}

	// 요청자와 수신자가 FamilyRelation의 멤버인지 확인
	private boolean isMembersMatched(FamilyRelation relation, MemberId requesterId, MemberId recipientId) {
		return (relation.getParentId().equals(requesterId) && relation.getChildId().equals(recipientId))
			|| (relation.getParentId().equals(recipientId) && relation.getChildId().equals(requesterId));
	}

	@Override
	public FamilyRelationId findRelationIdByMemberId(MemberId memberId) {
		return repository.findConnectedRelationByMemberId(memberId)
			.map(relation -> FamilyRelationId.of(relation.getId()))
			.orElseThrow(() -> BaseException.from(FamilyErrorCode.FAMILY_RELATION_NOT_FOUND));
	}
}
