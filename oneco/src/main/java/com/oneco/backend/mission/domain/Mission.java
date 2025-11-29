package com.oneco.backend.mission.domain;

import com.oneco.backend.global.entity.BaseTimeEntity;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.exception.MissionException;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Mission extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	// 가족 관계 ID (외래 키)
	private Long familyRelationId;

	@Embedded
	private MissionPeriod period;

	@Embedded
	private Reward reward;

	@Enumerated(EnumType.STRING)
	private MissionStatus status; // default: APPROVAL_REQUEST

	protected Mission() {
	} // JPA 기본 생성자

	private Mission(Long familyRelationId, MissionPeriod period, Reward reward) {

		// 미션 생성 시 가족을 선택하지 않은 경우 예외 처리
		if (familyRelationId == null) {
			throw MissionException.from(MissionErrorCode.FAMILY_RELATION_ID_CANNOT_BE_NULL);
		}

		this.familyRelationId = familyRelationId;
		this.period = period;
		this.reward = reward;
		this.status = MissionStatus.APPROVAL_REQUEST; // 미션 생성 시 기본 상태 값: 승인 요청
	}

	// 미션 생성 메서드
	public static Mission create(Long familyRelationId, MissionPeriod period, Reward reward) {
		return new Mission(familyRelationId, period, reward);
	}

	// === 미션 상태 변경 메서드 ===

	// 미션 승인 요청
	public void requestApproval() {
		// TODO: 상태 전환 가능 여부 검증 로직 추가
		this.status = MissionStatus.APPROVAL_REQUEST;
	}

	// 미션 승인 수락
	public void acceptApproval() {
		// 미션 승인 수락은 승인 요청 상태에서만 가능
		if (this.status != MissionStatus.APPROVAL_REQUEST) {
			throw MissionException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS);
		}
		this.status = MissionStatus.APPROVAL_ACCEPTED;
	}

	// 미션 승인 거절
	public void rejectApproval() {
		// 미션 승인 거절은 승인 요청 상태에서만 가능
		if (this.status != MissionStatus.APPROVAL_REQUEST) {
			throw MissionException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS);
		}
		this.status = MissionStatus.APPROVAL_REJECTED;
	}

	// 미션 진행 중
	public void markInProgress() {
		// 승인 수락 상태에서만 진행중으로 변경 가능하다.
		if (this.status != MissionStatus.APPROVAL_ACCEPTED) {
			throw MissionException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS);
		}
		this.status = MissionStatus.IN_PROGRESS;
	}

	// 미션 완료
	public void markCompleted() {
		// 미션 완료는 진행 중 상태에서만 가능하다.
		if (this.status != MissionStatus.IN_PROGRESS) {
			throw MissionException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS);
		}
		this.status = MissionStatus.COMPLETED;
	}

	// 미션 실패
	public void markFailed() {
		// 미션 실패는 진행 중 상태에서만 가능하다.
		if (this.status != MissionStatus.IN_PROGRESS) {
			throw MissionException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS);
		}
		this.status = MissionStatus.FAILED;
	}

	// 보상 요청
	public void requestReward() {
		// 보상 요청은 미션 완료 상태에서만 가능하다.
		if (this.status != MissionStatus.COMPLETED) {
			throw MissionException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS);
		}
		this.status = MissionStatus.REWARD_REQUESTED;
	}

	// 보상 완료
	public void completeReward() {
		// 보상 완료는 보상 요청 상태에서만 가능하다.
		if (this.status != MissionStatus.REWARD_REQUESTED) {
			throw MissionException.from(MissionErrorCode.INVALID_UPDATE_MISSION_STATUS);
		}
		this.status = MissionStatus.REWARD_COMPLETED;
	}
}
