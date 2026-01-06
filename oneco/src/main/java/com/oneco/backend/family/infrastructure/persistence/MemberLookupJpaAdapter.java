package com.oneco.backend.family.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.oneco.backend.family.application.port.out.MemberLookupPort;
import com.oneco.backend.member.domain.FamilyRole;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.member.infrastructure.persistence.MemberJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberLookupJpaAdapter implements MemberLookupPort {

	private final MemberJpaRepository memberJpaRepository;

	@Override
	public boolean exists(MemberId memberId) {
		return memberJpaRepository.existsById(memberId.getValue());
	}

	@Override
	public boolean isParent(MemberId memberId) {
		return memberJpaRepository.existsByIdAndFamilyRole(memberId.getValue(), FamilyRole.PARENT);
	}

	@Override
	public boolean isChild(MemberId memberId) {
		return memberJpaRepository.existsByIdAndFamilyRole(memberId.getValue(), FamilyRole.CHILD);
	}
}
