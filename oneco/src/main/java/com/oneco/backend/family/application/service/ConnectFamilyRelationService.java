package com.oneco.backend.family.application.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.family.application.dto.command.ConnectFamilyRelationCommand;
import com.oneco.backend.family.application.dto.result.FamilyRelationResult;
import com.oneco.backend.family.application.port.in.ConnectFamilyRelationUseCase;
import com.oneco.backend.family.application.port.out.FamilyRelationPersistencePort;
import com.oneco.backend.family.application.port.out.MemberLookupPort;
import com.oneco.backend.family.domain.exception.constant.FamilyErrorCode;
import com.oneco.backend.family.domain.relation.FamilyRelation;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectFamilyRelationService implements ConnectFamilyRelationUseCase {
	// ConnectFamilyRelationService는 AcceptInvitationService 에서만 호출된다.

	private static final int MAX_CHILDREN_LIMIT = 4;
	private static final int MAX_PARENTS_LIMIT = 2;

	private final FamilyRelationPersistencePort relationPort;
	private final MemberLookupPort memberLookupPort;

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public FamilyRelationResult connect(ConnectFamilyRelationCommand command) {
		MemberId parentId = MemberId.of(command.parentId());
		MemberId childId = MemberId.of(command.childId());

		// 1. 회원 존재 여부 검증 (Fail Fast)
		validateMembersExist(parentId, childId);

		// 2. 가족 구성원 수 제한 검증
		validateFamilySizeLimit(parentId, childId);

		// 3. 관계 생성 또는 재결합 처리
		FamilyRelation relation = relationPort.findByParentIdAndChildId(parentId, childId)
			.map(existingRelation -> {
				// 이미 가족 관계가 존재하면 재결합 시도 (CONNECTED 상태라면 내부에서 예외 발생)
				existingRelation.reconnect();
				return existingRelation;
			})
			.orElseGet(() -> {
				// 없으면 새로운 가족 관계 생성
				FamilyRelation newRelation = FamilyRelation.connect(parentId, childId);
				try {
					return relationPort.save(newRelation);
				} catch (DataIntegrityViolationException e) {
					// 동시성으로 인한 유니크 제약 위반을 도메인 예외로 변환
					throw BaseException.from(FamilyErrorCode.FAMILY_RELATION_ALREADY_EXISTS);
				}
			});

		return FamilyRelationResult.of(
			relation.getId(),
			relation.getParentId().getValue(),
			relation.getChildId().getValue(),
			relation.getStatus()
		);
	}

	// 부모와 자녀가 DB에 존재하는지 검증한다.
	private void validateMembersExist(MemberId parentId, MemberId childId) {
		if (!memberLookupPort.exists(parentId) || !memberLookupPort.exists(childId)) {
			throw BaseException.from(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
		}
	}

	// 부모와 자녀의 가족 구성원 수 제한을 검증한다.
	private void validateFamilySizeLimit(MemberId parentId, MemberId childId) {
		// 부모의 자녀 수 확인
		int currentChildCount = relationPort.countActiveChildrenByParentId(parentId);
		if (currentChildCount >= MAX_CHILDREN_LIMIT) {
			throw BaseException.from(
				FamilyErrorCode.FAMILY_RELATION_PARENT_CHILD_LIMIT_EXCEEDED,
				String.format("부모는 자녀를 %d명까지만 둘 수 있습니다. (현재: %d명)", MAX_CHILDREN_LIMIT, currentChildCount));
		}

		// 자녀의 부모 수 확인
		int currentParentCount = relationPort.countActiveParentsByChildId(childId);
		if (currentParentCount >= MAX_PARENTS_LIMIT) {
			throw BaseException.from(FamilyErrorCode.FAMILY_RELATION_CHILD_PARENT_LIMIT_EXCEEDED,
				String.format("자녀는 부모를 %d명까지만 둘 수 있습니다. (현재: %d명)", MAX_PARENTS_LIMIT, currentParentCount));
		}
	}
}
