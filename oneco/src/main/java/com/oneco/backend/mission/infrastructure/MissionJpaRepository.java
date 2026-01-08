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

	// 특정 카테고리의 최신 진행중인 미션 조회
	@Query("""
		select m from Mission m
		where (m.recipientId.value = :memberId OR m.requesterId.value = :memberId)
		  and m.categoryId.value = :categoryId
		  and m.status = :status
		order by m.createdAt desc
		""")
	List<Mission> findLatestActiveByCategory(
		@Param("memberId") Long memberId,
		@Param("categoryId") Long categoryId,
		@Param("status") MissionStatus status
	);

	// 최신 진행중인 미션 조회
	@Query("""
		select m from Mission m
		where (m.recipientId.value = :memberId OR m.requesterId.value = :memberId)
		  and m.status = :status
		order by m.createdAt desc
		""")
	List<Mission> findLatestActive(
		@Param("memberId") Long memberId,
		@Param("status") MissionStatus status
	);

	// 최신 진행중인 미션 1개 조회
	@Query("""
		select m from Mission m
		where (m.recipientId.value = :memberId OR m.requesterId.value = :memberId)
		  and m.status = :status
		order by m.createdAt desc
		""")
	List<Mission> findTop1LatestActive(
		@Param("memberId") Long memberId,
		@Param("status") MissionStatus status,
		Pageable pageable
	);

	// 특정 미션 ID와 회원으로 진행중인 미션 조회
	@Query("""
		select m from Mission m
		where (m.recipientId.value = :memberId OR m.requesterId.value = :memberId)
		  and m.id = :missionId
		  and m.status = :status
		""")
	Optional<Mission> findActiveByIdAndMember(
		@Param("memberId") Long memberId,
		@Param("missionId") Long missionId,
		@Param("status") MissionStatus status
	);


	@Query("SELECT m FROM Mission m " +
		"WHERE m.status = :status " +
		"AND m.period.endDate < :today")
	List<Mission> findAllOverdueMissions(
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

	long countByFamilyRelationIdValue(Long value);

	long countByFamilyRelationIdValueAndStatusIn(Long value, List<MissionStatus> statuses);

	// 가족관계와 진행중인 상태의 미션 존재 여부를 확인한다.
	boolean existsByFamilyRelationIdValueAndStatusIn(Long value, List<MissionStatus> statuses);
}
