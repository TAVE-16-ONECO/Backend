package com.oneco.backend.mission.infrastructure;

import org.springframework.stereotype.Component;

import com.oneco.backend.content.domain.dailycontent.CategoryId;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.mission.Mission;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MissionPersistenceAdapter implements MissionPersistencePort {

	private final MissionJpaRepository repository;

	@Override
	public Mission save(Mission mission) {
		return repository.save(mission);
	}

	@Override
	public boolean existsByFamilyRelationAndCategory(FamilyRelationId familyRelationId, CategoryId categoryId) {
		return repository.existsByFamilyRelationIdValueAndCategoryIdValue(familyRelationId.getValue(), categoryId.getValue());
	}

	@Override
	public Mission findById(Long missionId) {
		return repository.findById(missionId).orElseThrow(() -> BaseException.from(MissionErrorCode.MISSION_NOT_FOUND));
	}
}
