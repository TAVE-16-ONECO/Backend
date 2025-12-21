package com.oneco.backend.family.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.oneco.backend.family.application.port.out.MemberLookupPort;
import com.oneco.backend.member.domain.FamilyRole;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberLookupJpaAdapter implements MemberLookupPort {

	private final MemberRepository memberRepository;

	@Override
	public boolean exists(MemberId memberId) {
		return memberRepository.existsById(memberId.getValue());
	}

	@Override
	public boolean isParent(MemberId memberId) {
		return memberRepository.existsByIdAndFamilyRole(memberId.getValue(), FamilyRole.PARENT);
	}

	@Override
	public boolean isChild(MemberId memberId) {
		return memberRepository.existsByIdAndFamilyRole(memberId.getValue(), FamilyRole.CHILD);
	}
}
