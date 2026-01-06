package com.oneco.backend.family.presentation.response;

import com.oneco.backend.family.application.dto.result.FamilyRelationResult;

public record FamilyRelationResponse(
	Long familyRelationId,
	Long parentId,
	Long childId,
	String status
) {
	// 팩토리 메서드 - FamilyRelationResult(Application Layer) -> FamilyRelationResponse(Presentation Layer)
	public static FamilyRelationResponse from(FamilyRelationResult familyRelationResult) {
		return new FamilyRelationResponse(
			familyRelationResult.relationId(),
			familyRelationResult.parentId(),
			familyRelationResult.childId(),
			familyRelationResult.status().name()
		);
	}
}
