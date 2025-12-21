package com.oneco.backend.family.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.family.application.dto.command.AcceptInvitationCommand;
import com.oneco.backend.family.application.dto.command.ConnectFamilyRelationCommand;
import com.oneco.backend.family.application.dto.result.FamilyRelationResult;
import com.oneco.backend.family.application.port.in.AcceptInvitationUseCase;
import com.oneco.backend.family.application.port.in.ConnectFamilyRelationUseCase;
import com.oneco.backend.family.application.port.out.InvitationCodeStorePort;
import com.oneco.backend.family.application.port.out.MemberLookupPort;
import com.oneco.backend.family.domain.exception.constant.FamilyErrorCode;
import com.oneco.backend.family.domain.invitation.dto.FamilyInvitationRedisInfo;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AcceptInvitationService implements AcceptInvitationUseCase {

	private final InvitationCodeStorePort storePort;
	private final MemberLookupPort memberLookupPort;
	private final ConnectFamilyRelationUseCase connectUseCase;

	@Override
	public FamilyRelationResult accept(AcceptInvitationCommand command) {
		// === AcceptInvitationService의 책임 ===
		// (1) 초대코드 검증
		// (2) 부모/자녀 역할 기반으로 가족 관계 연결 커맨드 생성
		// (3) 가족 관계 연결 커맨드 실행
		// =======================================

		// (1) 초대코드 검증
		FamilyInvitationRedisInfo info = storePort.find(command.code())
			.orElseThrow(() -> BaseException.from(FamilyErrorCode.FAMILY_INVITATION_CODE_INVALID));

		// 초대자 ID 와 초대받는자 ID 가져오기
		MemberId inviterId = MemberId.of(info.getInviterId());
		MemberId inviteeId = MemberId.of(command.inviteeId());

		// (2) 부모/자녀 역할 기반으로 가족 관계 연결 커맨드 생성
		Long parentId;
		Long childId;
		if (memberLookupPort.isParent(inviterId) && memberLookupPort.isChild(inviteeId)) {
			parentId = inviterId.getValue();
			childId = inviteeId.getValue();
		} else if (memberLookupPort.isChild(inviterId) && memberLookupPort.isParent(inviteeId)) {
			parentId = inviteeId.getValue();
			childId = inviterId.getValue();
		} else {
			throw BaseException.from(
				FamilyErrorCode.FAMILY_RELATION_CONNECT_INVALID,
				"가족 관계는 부모-자녀 관계여야 합니다."
			);
		}

		// (3) 가족 관계 연결 커맨드 실행
		return connectUseCase.connect(new ConnectFamilyRelationCommand(parentId, childId));
	}
}
