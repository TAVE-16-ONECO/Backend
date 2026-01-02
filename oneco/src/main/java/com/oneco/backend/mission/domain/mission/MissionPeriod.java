package com.oneco.backend.mission.domain.mission;

import static lombok.AccessLevel.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

	public int openedDaySequenceExcludeWeekend(LocalDate today) {
		if (today == null) {
			throw BaseException.from(MissionErrorCode.TODAY_DATE_CANNOT_BE_NULL);
		}

		// 1) 미션 종료일 이후라면 종료일까지만 계산하여 결과값을 고정한다.
		LocalDate effectiveEnd = today.isAfter(endDate) ? endDate : today;

		// 2) 미션 시작 전이면 진행 일수는 0이다.
		if (effectiveEnd.isBefore(startDate)) {
			return 0;
		}

		// [동작 원리 예시]
		// 설정: startDate = 2024-01-01 (월), today = 2024-01-17 (수)
		// 총 기간: 1월 1일 ~ 1월 17일 (총 17일)

		// 3) 전체 일수 계산 (시작일 포함을 위해 +1)
		// totalDaysInclusive = 17
		long totalDaysInclusive = ChronoUnit.DAYS.between(startDate, effectiveEnd) + 1;

		// 4) 7일(1주일) 단위로 묶음 계산
		// fullWeeks = 17 / 7 = 2 (2주일 포함)
		// remainder = 17 % 7 = 3 (남은 3일: 1월 15일, 16일, 17일)
		long fullWeeks = totalDaysInclusive / 7;
		long remainder = totalDaysInclusive % 7;

		// 5) 완전한 주(7일)는 무조건 평일이 5일이므로 바로 곱셈 처리
		// weekdays = 2 * 5 = 10 (월~금 2회 반복)
		long weekdays = fullWeeks * 5;

		// 6) 남은 날짜(remainder)에 대해 루프를 돌며 평일 여부 확인
		// cursor는 1월 1일 + (2주 * 7일) = 1월 15일(월)부터 시작
		LocalDate cursor = startDate.plusDays(fullWeeks * 7);

		for (int i = 0; i < remainder; i++) {
			DayOfWeek dow = cursor.getDayOfWeek();

			// 1월 15일(월): 평일 → weekdays = 11
			// 1월 16일(화): 평일 → weekdays = 12
			// 1월 17일(수): 평일 → weekdays = 13
			if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
				weekdays++;
			}
			cursor = cursor.plusDays(1);
		}

		// 최종 반환값: 13 (주말 4일을 제외한 평일 수)
		return (int)weekdays;
	}
}
