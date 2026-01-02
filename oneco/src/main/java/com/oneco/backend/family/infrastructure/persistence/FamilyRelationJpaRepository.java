package com.oneco.backend.family.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.oneco.backend.family.domain.relation.FamilyRelation;
import com.oneco.backend.member.domain.MemberId;

public interface FamilyRelationJpaRepository extends JpaRepository<FamilyRelation, Long> {
	// 부모 ID와 자녀 ID를 이용하여 FamilyRelation 엔티티의 존재 여부를 확인하는 쿼리 메서드
	boolean existsByParentIdAndChildId(MemberId parentId, MemberId childId);

	// 부모 ID를 이용하여 FamilyRelation 엔티티에서 상태가 'CONNECTED'인 자녀의 수를 반환하는 쿼리 메서드
	@Query("select count(f) from FamilyRelation f where f.parentId = :parentId and f.status = 'CONNECTED'")
	int countActiveChildrenByParentId(MemberId parentId);

	// 자녀 ID를 이용하여 FamilyRelation 엔티티에서 상태가 'CONNECTED'인 부모의 수를 반환하는 쿼리 메서드
	@Query("select count(f) from FamilyRelation f where f.childId = :childId and f.status = 'CONNECTED'")
	int countActiveParentsByChildId(MemberId childId);

	// 부모 ID와 자녀 ID를 이용하여 해당하는 FamilyRelation 엔티티를 반환하는 쿼리 메서드
	@Lock(LockModeType.PESSIMISTIC_WRITE) // 낙관적 잠금 설정
	Optional<FamilyRelation> findByParentIdAndChildId(MemberId parentId, MemberId childId);

	// 멤버가 속한 가족 관계 조회 (부모 또는 자녀)
	@Query("select f from FamilyRelation f " +
		"where (f.parentId = :memberId or f.childId = :memberId) " +
		"and f.status = 'CONNECTED'")
	Optional<FamilyRelation> findConnectedRelationByMemberId(MemberId memberId);
}
