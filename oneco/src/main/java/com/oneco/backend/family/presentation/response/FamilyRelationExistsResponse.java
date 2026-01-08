package com.oneco.backend.family.presentation.response;

public record FamilyRelationExistsResponse(
	boolean hasFamilyRelation
) {
	// 팩토리 메서드 - boolean -> FamilyRelationExistsResponse
	public static FamilyRelationExistsResponse of(boolean hasFamilyRelation) {
		return new FamilyRelationExistsResponse(hasFamilyRelation);
	}
}
