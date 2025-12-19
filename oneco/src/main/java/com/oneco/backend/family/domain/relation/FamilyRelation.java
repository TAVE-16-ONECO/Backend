package com.oneco.backend.family.domain.relation;

import static lombok.AccessLevel.*;

import com.oneco.backend.family.domain.exception.constant.FamilyErrorCode;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "family_relation",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_family_relation_parent_child",
			columnNames = {"parent_id", "child_id"}
		)
	}
)
@Getter
@NoArgsConstructor(access = PROTECTED)
public class FamilyRelation extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Convert(converter = FamilyRelationIdConverter.class) // autoApply=true로 생략 가능하지만 명시적으로 작성
	private FamilyRelationId id;

	@Embedded
	@Column(name = "parent_id", nullable = false)
	private MemberId parentId;

	@Embedded
	@Column(name = "child_id", nullable = false)
	private MemberId childId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private RelationStatus status; // CONNECTED, DISCONNECTED

	private FamilyRelation(MemberId parentId, MemberId childId) {
		// 부모와 자녀가 동일한 멤버인지 검증
		if (parentId.equals(childId)) {
			throw BaseException.from(FamilyErrorCode.FAMILY_RELATION_INVALID_SAME_MEMBER);
		}

		this.parentId = parentId;
		this.childId = childId;
		this.status = RelationStatus.CONNECTED;
	}

	// todo 서비스 레이어에서 구현해야할 것들
	// todo actor 역할 검증: actor가 부모 또는 자녀인지 확인 -> service 레이어 담당

	public static FamilyRelation connect(MemberId parentId, MemberId childId) {
		return new FamilyRelation(parentId, childId);
	}

	public void disconnect(MemberId actor) {
		// todo: (1) actor가 null 인지 검증한다.
		// (1)을 domain layer 에서 검증하는 이유는 "도메인 엔티티가 스스로를 보호하도록 한다(dry)' 원칙을 근거한다.
		// todo: (2) actor가 FamilyRelation에 속한 멤버인지 검증한다.
		// todo: (3) FamilyRelation의 status가 DISCONNECTED 인지 검증한다.

		// (1) actor가 null 인지 검증한다.
		if (actor == null) {
			throw BaseException.from(FamilyErrorCode.MEMBER_ID_INVALID);
		}

		// (2) actor가 FamilyRelation에 속한 멤버인지 검증한다.
		if (!this.parentId.equals(actor) && !this.childId.equals(actor)) {
			throw BaseException.from(FamilyErrorCode.FAMILY_RELATION_DISCONNECT_FORBIDDEN);
		}

		// (3) FamilyRelation의 status가 DISCONNECTED 인지 검증한다.
		if (this.status == RelationStatus.DISCONNECTED) {
			throw BaseException.from(FamilyErrorCode.FAMILY_RELATION_ALREADY_DISCONNECTED);
		}

		this.status = RelationStatus.DISCONNECTED;
	}

}
