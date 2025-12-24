package com.oneco.backend.mission.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneco.backend.mission.domain.mission.Mission;

public interface MissionJpaRepository extends JpaRepository<Mission, Long> {

	// FamilyRelationId와 CategoryId로 미션 존재 여부 확인
	boolean existsByFamilyRelationIdValueAndCategoryIdValue(Long familyRelationId, Long categoryId);

}
