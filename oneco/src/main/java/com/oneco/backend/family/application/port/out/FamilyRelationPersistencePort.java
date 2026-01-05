package com.oneco.backend.family.application.port.out;

import java.util.Optional;

import com.oneco.backend.family.domain.relation.FamilyRelation;
import com.oneco.backend.member.domain.MemberId;

public interface FamilyRelationPersistencePort {
	FamilyRelation save(FamilyRelation relation);

	Optional<FamilyRelation> findById(Long relationId);

	int countActiveChildrenByParentId(MemberId parentId);

	int countActiveParentsByChildId(MemberId childId);

	Optional<FamilyRelation> findByParentIdAndChildId(MemberId parentId, MemberId childId);

	boolean existsByMemberId(Long memberId);
}
