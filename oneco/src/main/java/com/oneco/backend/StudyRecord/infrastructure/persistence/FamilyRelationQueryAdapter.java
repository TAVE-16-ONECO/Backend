package com.oneco.backend.StudyRecord.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.result.MemberItem;
import com.oneco.backend.StudyRecord.application.port.out.FamilyRelationQueryPort;
import com.oneco.backend.family.domain.relation.FamilyRelation;
import com.oneco.backend.family.infrastructure.persistence.FamilyRelationJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyRelationQueryAdapter implements FamilyRelationQueryPort {

	private final FamilyRelationJpaRepository familyRelationJpaRepository;
	@Override
	@Transactional(readOnly = true)
	public List<MemberItem> findChildIdsByParentId(Long parentId) {
		return familyRelationJpaRepository.findAllConnectedChildrenByParentId(parentId);
	}
}
