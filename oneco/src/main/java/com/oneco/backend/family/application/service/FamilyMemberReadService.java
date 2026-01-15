package com.oneco.backend.family.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.family.application.dto.result.FamilyMembersResult;
import com.oneco.backend.family.application.dto.result.FamilyMembersResult.FamilyMemberResult;
import com.oneco.backend.family.application.port.in.GetFamilyMembersUseCase;
import com.oneco.backend.family.application.port.out.FamilyRelationPersistencePort;
import com.oneco.backend.family.application.port.out.MemberLookupPort;
import com.oneco.backend.family.domain.exception.constant.FamilyErrorCode;
import com.oneco.backend.family.domain.relation.FamilyRelation;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.Member;
import com.oneco.backend.member.domain.MemberId;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FamilyMemberReadService implements GetFamilyMembersUseCase {

	private final FamilyRelationPersistencePort relationPort;
	private final MemberLookupPort memberLookupPort;

	@Override
	public FamilyMembersResult getFamilyMembers(MemberId memberId) {
		List<FamilyRelation> relations = relationPort.findConnectedRelationsByMemberId(memberId);

		// 가족 관계가 없는 경우 예외 처리
		if (relations.isEmpty()) {
			throw BaseException.from(FamilyErrorCode.FAMILY_RELATION_NOT_FOUND);
		}

		// 상대방 멤버 정보 조회 및 결과 생성
		List<FamilyMemberResult> members = relations.stream()
			.map(relation -> resolveCounterpart(relation, memberId))
			.toList();

		return FamilyMembersResult.of(members);
	}

	// 요청자의 상대방 멤버 정보를 조회하는 메서드
	private FamilyMemberResult resolveCounterpart(FamilyRelation relation, MemberId requesterId) {

		// 요청자가 부모인지 자식인지에 따라 상대방 멤버 ID 결정
		MemberId counterpartId = relation.getParentId().equals(requesterId)
			? relation.getChildId()
			: relation.getParentId();

		Member counterpart = memberLookupPort.findById(counterpartId)
			.orElseThrow(() -> BaseException.from(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND));

		return FamilyMemberResult.of(
			counterpart.getId(),
			counterpart.getNickname(),
			counterpart.getProfileImageUrl()
		);
	}
}
