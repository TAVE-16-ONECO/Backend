package com.oneco.backend.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneco.backend.member.domain.FamilyRole;
import com.oneco.backend.member.domain.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
	// Member의 familyRole이 'PARENT' 인지 확인
	boolean existsByIdAndFamilyRole(Long memberId, FamilyRole familyRole);

}

