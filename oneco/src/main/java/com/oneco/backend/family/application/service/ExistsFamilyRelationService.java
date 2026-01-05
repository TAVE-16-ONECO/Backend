package com.oneco.backend.family.application.service;

import org.springframework.stereotype.Service;

import com.oneco.backend.family.application.port.in.ExistsFamilyRelationUseCase;
import com.oneco.backend.family.application.port.out.FamilyRelationPersistencePort;
import com.oneco.backend.family.presentation.response.FamilyRelationExists;
import com.oneco.backend.member.domain.MemberId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExistsFamilyRelationService implements ExistsFamilyRelationUseCase {

	private final FamilyRelationPersistencePort relationPort;

	// 회원의 가족 관계 존재 여부 확인 구현
	@Override
	public FamilyRelationExists existsFamilyRelation(MemberId memberId) {
		return new FamilyRelationExists(relationPort.existsByMemberId(memberId));
	}
}
