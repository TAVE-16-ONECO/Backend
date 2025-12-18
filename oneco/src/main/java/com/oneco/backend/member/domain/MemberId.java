package com.oneco.backend.member.domain;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.exception.constant.MemberErrorCode;

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
public class MemberId {

	@Column(name = "member_id", nullable = false)
	private Long value;

	private MemberId(Long value) {
		if (value == null || value <= 0) {
			throw BaseException.from(MemberErrorCode.INVALID_MEMBER_ID);
		}
		this.value = value;
	}

	public static MemberId of(Long value) {
		return new MemberId(value);
	}
}

