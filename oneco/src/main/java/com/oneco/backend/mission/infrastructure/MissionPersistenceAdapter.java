package com.oneco.backend.mission.infrastructure;

import org.springframework.stereotype.Component;

import com.oneco.backend.content.domain.dailycontent.CategoryId;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
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
}
