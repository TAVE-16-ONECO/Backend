package com.oneco.backend.family.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
	Optional<FamilyRelation> findByParentIdAndChildId(MemberId parentId, MemberId childId);
}
