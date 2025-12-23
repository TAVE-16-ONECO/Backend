package com.oneco.backend.family.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.family.application.dto.command.DisconnectFamilyRelationCommand;
import com.oneco.backend.family.application.dto.result.FamilyRelationResult;
import com.oneco.backend.family.application.port.in.DisconnectFamilyRelationUseCase;
import com.oneco.backend.family.application.port.out.FamilyRelationPersistencePort;
import com.oneco.backend.family.domain.exception.constant.FamilyErrorCode;
import com.oneco.backend.family.domain.relation.FamilyRelation;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DisconnectFamilyRelationService implements DisconnectFamilyRelationUseCase {
	private final FamilyRelationPersistencePort relationPort;

	@Override
	public FamilyRelationResult disconnect(DisconnectFamilyRelationCommand command) {
		FamilyRelation relation = relationPort.findById(command.relationId())
			.orElseThrow(() -> BaseException.from(FamilyErrorCode.FAMILY_RELATION_NOT_FOUND));

		relation.disconnect(MemberId.of(command.actorId()));

		return FamilyRelationResult.of(
			relation.getId(),
			relation.getParentId().getValue(),
			relation.getChildId().getValue(),
			relation.getStatus()
		);
	}
}
