package com.oneco.backend.family.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.oneco.backend.family.application.port.out.FamilyRelationPersistencePort;
import com.oneco.backend.family.domain.relation.FamilyRelation;
import com.oneco.backend.member.domain.MemberId;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyRelationPersistenceAdapter implements FamilyRelationPersistencePort {
	private final FamilyRelationJpaRepository repository;

	@Override
	public FamilyRelation save(FamilyRelation relation) {
		return repository.save(relation);
	}

	@Override
	public Optional<FamilyRelation> findById(Long relationId) {
		return repository.findById(relationId);
	}

	@Override
	public int countActiveChildrenByParentId(MemberId parentId) {
		return repository.countActiveChildrenByParentId(parentId);
	}

	@Override
	public int countActiveParentsByChildId(MemberId childId) {
		return repository.countActiveParentsByChildId(childId);
	}

	@Override
	public Optional<FamilyRelation> findByParentIdAndChildId(MemberId parentId, MemberId childId) {
		return repository.findByParentIdAndChildId(parentId, childId);
	}

}
