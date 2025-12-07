package com.oneco.backend.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.UserErrorCode;
import com.oneco.backend.member.application.MemberService;
import com.oneco.backend.member.domain.Member;
import com.oneco.backend.member.domain.MemberRepository;
import com.oneco.backend.member.domain.MemberStatus;
import com.oneco.backend.member.domain.SystemRole;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;

	@Test
	void findByIdOrThrow_whenMemberExists_returnsMember() {
		Member member = Member.createForOnboarding("profile", "nick", SystemRole.USER);
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

		Member found = memberService.findByIdOrThrow(1L);

		assertSame(member, found);
	}

	@Test
	void findByIdOrThrow_whenMissing_throwsUserNotFound() {
		when(memberRepository.findById(10L)).thenReturn(Optional.empty());

		BaseException ex = assertThrows(BaseException.class, () -> memberService.findByIdOrThrow(10L));
		assertEquals(UserErrorCode.USER_NOT_FOUND.getCode(), ex.getCode());
	}


	@Test
	void canCompleteOnboarding_allowsOnlyOnboardingStatus() {
		assertTrue(memberService.canCompleteOnboarding(MemberStatus.ONBOARDING));
		assertFalse(memberService.canCompleteOnboarding(MemberStatus.ACTIVE));
	}
}
