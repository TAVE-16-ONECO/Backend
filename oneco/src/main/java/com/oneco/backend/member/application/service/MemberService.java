package com.oneco.backend.member.application.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.application.port.in.MemberUseCase;
import com.oneco.backend.member.domain.exception.constant.MemberErrorCode;
import com.oneco.backend.member.domain.Member;
import com.oneco.backend.member.infrastructure.persistence.MemberJpaRepository;
import com.oneco.backend.member.domain.MemberStatus;
import com.oneco.backend.member.presentation.response.MemberInfoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberUseCase {
	private final MemberJpaRepository memberJpaRepository;

	@Transactional(readOnly = true)
	public Member findByIdOrThrow(Long memberId) {
		Objects.requireNonNull(memberId, "memberId must not be null");
		return memberJpaRepository.findById(memberId)
			.orElseThrow(() -> BaseException.from(MemberErrorCode.MEMBER_NOT_FOUND));
	}

	public boolean canCompleteOnboarding(MemberStatus status) {
		return status == MemberStatus.ONBOARDING;
	}

	@Override
	public MemberInfoResponse getMemberInfo(Long memberId) {
		Member member = memberJpaRepository.findById(memberId).
			orElseThrow(() -> BaseException.from(
				MemberErrorCode.MEMBER_NOT_FOUND, "MemberId: " + memberId)
			);
		return MemberInfoResponse.of(
			member.getId(),
			member.getFamilyRole().toString(),
			member.getName(),
			member.getNickname(),
			member.getEmail(),
			member.getProfileImageUrl());
	}
}
