package com.oneco.backend.member.application.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.application.dto.result.MemberInfoResult;
import com.oneco.backend.member.application.port.in.MemberUseCase;
import com.oneco.backend.member.application.port.out.MemberPersistencePort;
import com.oneco.backend.member.domain.exception.constant.MemberErrorCode;
import com.oneco.backend.member.domain.Member;
import com.oneco.backend.member.infrastructure.persistence.MemberJpaRepository;
import com.oneco.backend.member.domain.MemberStatus;
import com.oneco.backend.member.presentation.response.MemberInfoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberUseCase {
	private final MemberPersistencePort memberPort;

	@Transactional(readOnly = true)
	public Member findByIdOrThrow(Long memberId) {
		Objects.requireNonNull(memberId, "memberId must not be null");
		return memberPort.findById(memberId)
			.orElseThrow(() -> BaseException.from(MemberErrorCode.MEMBER_NOT_FOUND));
	}

	public boolean canCompleteOnboarding(MemberStatus status) {
		return status == MemberStatus.ONBOARDING;
	}

	@Override
	@Transactional(readOnly = true)
	public MemberInfoResult getMemberInfo(Long memberId) {
		Member member = memberPort.findById(memberId).
			orElseThrow(() -> BaseException.from(
				MemberErrorCode.MEMBER_NOT_FOUND, "MemberId: " + memberId)
			);
		return MemberInfoResult.of(
			member.getId(),
			member.getFamilyRole(),
			member.getName(),
			member.getNickname(),
			member.getEmail(),
			member.getProfileImageUrl());
	}
}
