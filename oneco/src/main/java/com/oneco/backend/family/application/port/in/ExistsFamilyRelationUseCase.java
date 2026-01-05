package com.oneco.backend.family.application.port.in;

import com.oneco.backend.family.presentation.response.FamilyRelationExists;

public interface ExistsFamilyRelationUseCase {

	// 회원의 가족 관계 존재 여부 확인 메서드
	FamilyRelationExists existsFamilyRelation(Long memberId);
}
