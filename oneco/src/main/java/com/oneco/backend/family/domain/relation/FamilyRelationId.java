package com.oneco.backend.family.domain.relation;

import java.io.Serializable;

import com.oneco.backend.family.domain.exception.constant.FamilyErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyRelationId implements Serializable {

	@Column(name = "family_relation_id", nullable = false)
	private Long value;

	private FamilyRelationId(Long value) {
		if (value == null || value <= 0) {
			throw BaseException.from(FamilyErrorCode.INVALID_FAMILY_RELATION_ID);
		}
		this.value = value;
	}

	public static FamilyRelationId of(Long value) {
		return new FamilyRelationId(value);
	}
}
