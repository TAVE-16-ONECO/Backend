package com.oneco.backend.mission.domain;

import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
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
			throw BaseException.from(MissionErrorCode.INVALID_MISSION_ID, "입력값=" + value);
		}
		this.value = value;
	}

	public static MissionId of(Long value) {
		return new MissionId(value);
	}

}
