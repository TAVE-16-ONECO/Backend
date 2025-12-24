package com.oneco.backend.mission.application.port.out;

import com.oneco.backend.content.domain.dailycontent.CategoryId;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.mission.domain.mission.Mission;

public interface MissionPersistencePort {
	Mission save(Mission mission);

	boolean existsByFamilyRelationAndCategory(FamilyRelationId familyRelationId, CategoryId categoryId);

	Mission findById(Long missionId);
}
