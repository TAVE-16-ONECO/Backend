package com.oneco.backend.family.application.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.family.application.dto.command.IssueInvitationCommand;
import com.oneco.backend.family.application.dto.result.IssueInvitationResult;
import com.oneco.backend.family.application.port.in.IssueInvitationUseCase;
import com.oneco.backend.family.application.port.out.InvitationCodeGenerator;
import com.oneco.backend.family.application.port.out.InvitationCodeStorePort;
import com.oneco.backend.family.application.port.out.MemberLookupPort;
import com.oneco.backend.family.domain.exception.constant.FamilyErrorCode;
import com.oneco.backend.family.domain.invitation.dto.FamilyInvitationRedisInfo;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IssueInvitationService implements IssueInvitationUseCase {

	private static final long DEFAULT_EXPIRES_IN_SECONDS = 86400L; // 24 hours

	private final InvitationCodeGenerator codeGenerator;
	private final InvitationCodeStorePort storePort;
	private final MemberLookupPort memberLookupPort;

	@Override
	public IssueInvitationResult issue(IssueInvitationCommand command) {
		MemberId inviterId = MemberId.of(command.inviterId());

		// 초대자 존재 여부 검증
		if (!memberLookupPort.exists(inviterId)) {
			throw BaseException.from(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
		}

		// =============================
		// 기존에 발급된 초대 코드가 있는지 확인
		// =============================
		Optional<String> existingCode = storePort.findCodeByInviterId(inviterId.getValue());
		if (existingCode.isPresent()) {
			String code = existingCode.get(); // 기존 초대 코드를 재사용한다.
			// 남은 유효 기간 조회(조회 실패 시, 기본 만료시간으로 초기화 한다)
			long expiresInSeconds = storePort.getRemainingSeconds(code).orElse(DEFAULT_EXPIRES_IN_SECONDS);
			return new IssueInvitationResult(code, expiresInSeconds);
		}

		// 초대 코드 객체 생성
		String code = codeGenerator.generate();
		FamilyInvitationRedisInfo info = FamilyInvitationRedisInfo.builder()
			.inviterId(inviterId.getValue())
			.build();

		// 초대 코드 저장
		storePort.save(code, info, Duration.ofSeconds(DEFAULT_EXPIRES_IN_SECONDS));

		// 결과 반환
		return new IssueInvitationResult(code, DEFAULT_EXPIRES_IN_SECONDS);
	}

}
