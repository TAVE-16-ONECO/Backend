package com.oneco.backend.mission.application.port.out;

import java.time.LocalDate;
import java.util.List;

import com.oneco.backend.category.domain.category.CategoryId;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.domain.mission.MissionStatus;

public interface MissionPersistencePort {
	Mission save(Mission mission);

	boolean existsByFamilyRelationAndCategory(FamilyRelationId familyRelationId, CategoryId categoryId);

	Mission findById(Long missionId);

	// 진행중인 미션들 중, 마감기한이 지난 미션들을 모두 조회한다.
	List<Mission> findAllOverdueMissions(LocalDate today);

	// 상태는 IN_PROGRESS, APPROVAL_ACCEPTED, APPROVAL_REQUEST일 때 조회
	List<Mission> findByFamilyRelationAndInProgressStatus(FamilyRelationId relationId, Long lastId, int size);

	// 상태는 COMPLETED, REJECTED, EXPIRED, CANCELED, FAILED 일 때 조회
	List<Mission> findByFamilyRelationAndFinishedStatus(FamilyRelationId relationId, Long lastId, int size);

	// 오늘이 미션 시작일인 모든 미션을 조회한다.
	List<Mission> findAllMissionsStartingToday(LocalDate today);

	// 가족관계를 기반으로 미션 개수를 조회한다.
	long countMissionsByFamilyRelation(FamilyRelationId relationId);

	// 가족관계를 기반으로 주어진 상태 목록의 미션 개수를 조회한다.
	long countMissionsByFamilyRelationAndStatuses(FamilyRelationId relationId, List<MissionStatus> statuses);

	// 가족관계와 진행중인 상태의 미션 존재 여부를 확인한다.
	boolean existsByFamilyRelationAndInProgressStatus(FamilyRelationId relationId, List<MissionStatus> statuses);
}
