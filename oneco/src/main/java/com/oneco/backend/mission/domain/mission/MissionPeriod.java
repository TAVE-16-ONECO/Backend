package com.oneco.backend.mission.domain.mission;

import static lombok.AccessLevel.*;

import java.time.LocalDate;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
public class MissionPeriod {

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;
	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	private MissionPeriod(LocalDate startDate, LocalDate endDate) {

		// 미션 시작일이나 종료일이 null인 경우 예외 처리
		if (startDate == null || endDate == null) {
			throw BaseException.from(MissionErrorCode.MISSION_TIME_CANNOT_BE_NULL);
		}
		// 미션 시작일이 종료일보다 이후인 경우 예외 처리
		if (startDate.isAfter(endDate)) {
			throw BaseException.from(MissionErrorCode.INVALID_MISSION_TIME_ORDER);
		}

		this.startDate = startDate;
		this.endDate = endDate;
	}

	public static MissionPeriod of(LocalDate startDate, LocalDate endDate) {
		return new MissionPeriod(startDate, endDate);
	}

}
