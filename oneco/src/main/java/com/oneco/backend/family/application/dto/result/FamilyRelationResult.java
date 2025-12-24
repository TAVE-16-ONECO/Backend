package com.oneco.backend.family.application.dto.result;

import com.oneco.backend.family.domain.relation.RelationStatus;

public record FamilyRelationResult(
	Long relationId,
	Long parentId,
	Long childId,
	RelationStatus status
) {
	public static FamilyRelationResult of(Long id, Long parentId, Long childId, RelationStatus status) {
		return new FamilyRelationResult(id, parentId, childId, status);
	}
}
