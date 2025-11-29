package com.oneco.backend.mission.domain;

import java.time.LocalDate;

import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.exception.MissionException;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class MissionPeriod {

	private LocalDate startDate;
	private LocalDate endDate;

	protected MissionPeriod() {
	}

	private MissionPeriod(LocalDate startDate, LocalDate endDate) {


		// 미션 시작일이나 종료일이 null인 경우 예외 처리
		if (startDate == null || endDate == null) {
			throw MissionException.from(MissionErrorCode.MISSION_TIME_CANNOT_BE_NULL);
		}
		// 미션 시작일이 종료일보다 이후인 경우 예외 처리
		if (startDate.isAfter(endDate)) {
			throw MissionException.from(MissionErrorCode.INVALID_MISSION_TIME_ORDER);
		}

		this.startDate = startDate;
		this.endDate = endDate;
	}

	public static MissionPeriod of(LocalDate startDate, LocalDate endDate) {
		return new MissionPeriod(startDate, endDate);
	}

}
