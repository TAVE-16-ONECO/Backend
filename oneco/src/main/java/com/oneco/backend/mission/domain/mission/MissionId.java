package com.oneco.backend.mission.domain.mission;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionId {

	private Long value;

	private MissionId(Long value) {
		if (value == null || value <= 0) {
			throw BaseException.from(
				MissionErrorCode.INVALID_MISSION_ID,
				"미션 ID는 null 이거나 0 이하일 수 없습니다. " + "입력값=" + value
			);
		}
		this.value = value;
	}

	public static MissionId of(Long value) {
		return new MissionId(value);
	}

}