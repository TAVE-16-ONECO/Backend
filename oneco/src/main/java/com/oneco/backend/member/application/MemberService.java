package com.oneco.backend.member.application;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.UserErrorCode;
import com.oneco.backend.member.domain.Member;
import com.oneco.backend.member.domain.MemberRepository;
import com.oneco.backend.member.domain.MemberStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;

	@Transactional(readOnly = true)
	public Member findByIdOrThrow(Long memberId){
		Objects.requireNonNull(memberId, "memberId must not be null");
		return memberRepository.findById(memberId)
			.orElseThrow(()-> BaseException.from(UserErrorCode.USER_NOT_FOUND));
	}

	public boolean canCompleteOnboarding(MemberStatus status) {
		return status == MemberStatus.ONBOARDING;
	}
}
