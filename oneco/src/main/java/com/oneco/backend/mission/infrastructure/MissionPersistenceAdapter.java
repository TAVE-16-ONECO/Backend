package com.oneco.backend.mission.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.oneco.backend.category.domain.category.CategoryId;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.domain.mission.MissionStatus;

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
		return repository.existsByFamilyRelationIdValueAndCategoryIdValue(familyRelationId.getValue(),
			categoryId.getValue());
	}

	@Override
	public Mission findById(Long missionId) {
		return repository.findById(missionId).orElseThrow(() -> BaseException.from(MissionErrorCode.MISSION_NOT_FOUND));
	}

	// 진행중인 미션들 중, 마감기한이 지난 미션들을 모두 조회한다.
	@Override
	public List<Mission> findAllOverdueMissions(LocalDate today) {
		return repository.findAllOverdueMissions(MissionStatus.IN_PROGRESS, today);
	}

	// 상태는 IN_PROGRESS, APPROVAL_ACCEPTED, APPROVAL_REQUEST일 때 조회
	@Override
	public List<Mission> findByFamilyRelationAndInProgressStatus(FamilyRelationId relationId, Long lastId, int size) {
		return repository.findByFamilyRelationAndInProgressStatus(
			relationId.getValue(),
			MissionStatus.IN_PROGRESS,
			MissionStatus.APPROVAL_ACCEPTED,
			MissionStatus.APPROVAL_REQUEST,
			lastId,
			PageRequest.of(0, size)
		);
	}

	// 상태는 APPROVAL_REJECTED, COMPLETED, FAILED, REWARD_REQUESTED, REWARD_COMPLETED 일 때 조회
	@Override
	public List<Mission> findByFamilyRelationAndFinishedStatus(FamilyRelationId relationId, Long lastId, int size) {
		return repository.findByFamilyRelationAndFinishedStatus(
			relationId.getValue(),
			MissionStatus.APPROVAL_REJECTED,
			MissionStatus.COMPLETED,
			MissionStatus.FAILED,
			MissionStatus.REWARD_REQUESTED,
			MissionStatus.REWARD_COMPLETED,
			lastId,
			PageRequest.of(0, size)
		);
	}

	// 오늘이 미션 시작일인 모든 미션을 조회한다.
	@Override
	public List<Mission> findAllMissionsStartingToday(LocalDate today) {
		return repository.findAllByStartDate(today, MissionStatus.APPROVAL_ACCEPTED);
	}

	@Override
	public long countMissionsByFamilyRelation(FamilyRelationId relationId) {
		return repository.countByFamilyRelationIdValue(relationId.getValue());
	}

	@Override
	public long countMissionsByFamilyRelationAndStatuses(FamilyRelationId relationId, List<MissionStatus> statuses) {
		return repository.countByFamilyRelationIdValueAndStatusIn(relationId.getValue(), statuses);
	}

	@Override
	public boolean existsByFamilyRelationAndInProgressStatus(FamilyRelationId relationId, List<MissionStatus> statuses) {
		return repository.existsByFamilyRelationIdValueAndStatusIn(relationId.getValue(), statuses);
	}
}
