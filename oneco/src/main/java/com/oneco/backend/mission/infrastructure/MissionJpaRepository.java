package com.oneco.backend.mission.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.domain.mission.MissionStatus;

public interface MissionJpaRepository extends JpaRepository<Mission, Long> {

	// FamilyRelationId와 CategoryId로 미션 존재 여부 확인
	boolean existsByFamilyRelationIdValueAndCategoryIdValue(Long familyRelationId, Long categoryId);

	@Query("""
		select m from Mission m
		where m.recipientId.value = :memberId
		  and m.categoryId.value = :categoryId
		  and m.status = :status
		order by m.createdAt desc
		""")
	Optional<Mission> findLatestActiveForRecipient(
		@Param("memberId") Long memberId,
		@Param("categoryId") Long categoryId,
		@Param("status") MissionStatus status
	);

	@Query("SELECT m FROM Mission m " +
		"WHERE m.status = :status " +
		"AND m.period.endDate < :today")
	public List<Mission> findAllOverdueMissions(
		@Param("status") MissionStatus status,
		@Param("today") LocalDate today
	);

	@Query("SELECT m FROM Mission m " +
		"WHERE m.familyRelationId.value = :value " +
		"AND (m.status = :missionStatus " +
		"OR m.status = :missionStatus1 " +
		"OR m.status = :missionStatus2) " +
		"AND m.id < :lastId " +
		"ORDER BY m.id DESC")
	List<Mission> findByFamilyRelationAndInProgressStatus(
		Long value,
		MissionStatus missionStatus,
		MissionStatus missionStatus1,
		MissionStatus missionStatus2,
		Long lastId,
		Pageable pageable);

	@Query("SELECT m FROM Mission m " +
		"WHERE m.familyRelationId.value = :value " +
		"AND (m.status = :missionStatus " +
		"OR m.status = :missionStatus1 " +
		"OR m.status = :missionStatus2 " +
		"OR m.status = :missionStatus3 " +
		"OR m.status = :missionStatus4) " +
		"AND m.id < :lastId " +
		"ORDER BY m.id DESC")
	List<Mission> findByFamilyRelationAndFinishedStatus(
		Long value,
		MissionStatus missionStatus,
		MissionStatus missionStatus1,
		MissionStatus missionStatus2,
		MissionStatus missionStatus3,
		MissionStatus missionStatus4,
		Long lastId,
		Pageable pageable);

	// 오늘이 미션 시작일인 모든 미션을 조회한다.
	@Query("SELECT m FROM Mission m " +
		"WHERE m.period.startDate = :today " +
		"AND m.status = :status")
	List<Mission> findAllByStartDate(
		@Param("today") LocalDate today,
		@Param("status") MissionStatus status
	);
}
