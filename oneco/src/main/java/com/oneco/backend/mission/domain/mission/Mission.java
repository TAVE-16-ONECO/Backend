package com.oneco.backend.mission.domain.mission;

import static lombok.AccessLevel.*;

import com.oneco.backend.content.domain.dailycontent.CategoryId;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.global.entity.BaseTimeEntity;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Mission extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "requester_id", nullable = false))
	private MemberId requesterId; // 미션을 생성한 멤버 ID

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "recipient_id", nullable = false))
	private MemberId recipientId; // 요청 받는 사람 멤버 ID

	// 가족 관계 ID (외래 키)
	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "family_relation_id", nullable = false))
	private FamilyRelationId familyRelationId;

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "category_id", nullable = false))
	private CategoryId categoryId;

	@Embedded
	@Column(nullable = false)
	private MissionPeriod period;

	@Embedded
	private Reward reward;

	@Enumerated(EnumType.STRING)
	private MissionStatus status; // default: APPROVAL_REQUEST

	private Mission(
		MemberId requesterId,
		MemberId recipientId,
		FamilyRelationId familyRelationId,
		CategoryId categoryId,
		MissionPeriod period,
		Reward reward
	) {

		// requesterId null인 경우 예외 처리
		if (requesterId == null) {
			throw BaseException.from(
				MissionErrorCode.MEMBER_ID_CANNOT_BE_NULL,
				"요청자 ID는 null일 수 없습니다."
			);
		}

		if (recipientId == null) {
			throw BaseException.from(
				MissionErrorCode.MEMBER_ID_CANNOT_BE_NULL,
				"수신자 ID는 null일 수 없습니다."
			);
		}

		// familyRelationId가 null인 경우 예외 처리
		if (familyRelationId == null) {
			throw BaseException.from(MissionErrorCode.FAMILY_RELATION_ID_CANNOT_BE_NULL);
		}

		// categoryId가 null인 경우 예외 처리
		if (categoryId == null) {
			throw BaseException.from(MissionErrorCode.CATEGORY_ID_CANNOT_BE_NULL);
		}

		if (period == null) {
			throw BaseException.from(MissionErrorCode.MISSION_PERIOD_CANNOT_BE_NULL);
		}

		this.requesterId = requesterId;
		this.recipientId = recipientId;
		this.familyRelationId = familyRelationId;
		this.categoryId = categoryId;
		this.period = period;
		this.reward = reward;
		this.status = MissionStatus.APPROVAL_REQUEST; // 미션 생성 시 기본 상태 값: 승인 요청
	}

	// 미션 생성 메서드
	public static Mission create(
		MemberId requesterId,
		MemberId recipientId,
		FamilyRelationId familyRelationId,
		CategoryId categoryId,
		MissionPeriod period,
		Reward reward
	) {
		return new Mission(requesterId, recipientId, familyRelationId, categoryId, period, reward);
	}

	// === 미션 상태 변경 메서드 ===

	// 미션 승인 요청
	public void requestApproval() {
		// TODO: 상태 전환 가능 여부 검증 로직 추가 ->
		this.status = MissionStatus.APPROVAL_REQUEST;
	}

	// 미션 승인 수락
	public void acceptApproval() {
		// 미션 승인 수락은 승인 요청 상태에서만 가능
		if (this.status != MissionStatus.APPROVAL_REQUEST) {
			throw BaseException.from(
				MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"미션 승인 수락은 승인 요청 상태에서만 가능합니다."
			);
		}
		this.status = MissionStatus.APPROVAL_ACCEPTED;
	}

	// 미션 승인 거절
	public void rejectApproval() {
		// 미션 승인 거절은 승인 요청 상태에서만 가능
		if (this.status != MissionStatus.APPROVAL_REQUEST) {
			throw BaseException.from(
				MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"미션 승인 거절은 승인 요청 상태에서만 가능합니다."
			);
		}
		this.status = MissionStatus.APPROVAL_REJECTED;
	}

	// 미션 진행 중
	public void markInProgress() {
		// 승인 수락 상태에서만 진행중으로 변경 가능하다.
		if (this.status != MissionStatus.APPROVAL_ACCEPTED) {
			throw BaseException.from(
				MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"미션 진행은 승인 수락 상태에서만 가능합니다.");
		}
		this.status = MissionStatus.IN_PROGRESS;
	}

	// 미션 완료
	public void markCompleted() {
		// 미션 완료는 진행 중 상태에서만 가능하다.
		if (this.status != MissionStatus.IN_PROGRESS) {
			throw BaseException.from(
				MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"미션 완료는 진행 중 상태에서만 가능합니다."
			);
		}
		this.status = MissionStatus.COMPLETED;
	}

	// 미션 실패
	public void markFailed() {
		// 미션 실패는 진행 중 상태에서만 가능하다.
		if (this.status != MissionStatus.IN_PROGRESS) {
			throw BaseException.from(
				MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"미션 실패는 진행 중 상태에서만 가능합니다."
			);
		}
		this.status = MissionStatus.FAILED;
	}

	// 보상 요청
	public void requestReward() {
		// 보상 요청은 미션 완료 상태에서만 가능하다.
		if (this.status != MissionStatus.COMPLETED) {
			throw BaseException.from(
				MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"보상 요청은 미션 완료 상태에서만 가능합니다.");
		}
		this.status = MissionStatus.REWARD_REQUESTED;
	}

	// 보상 완료
	public void completeReward() {
		// 보상 완료는 보상 요청 상태에서만 가능하다.
		if (this.status != MissionStatus.REWARD_REQUESTED) {
			throw BaseException.from(
				MissionErrorCode.INVALID_UPDATE_MISSION_STATUS,
				"보상 완료는 보상 요청 상태에서만 가능합니다.");
		}
		this.status = MissionStatus.REWARD_COMPLETED;
	}
}
