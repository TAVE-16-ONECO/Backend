package com.oneco.backend.mission.domain.mission;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MissionDateCalculator {

	private MissionDateCalculator() {
	}

	// 오늘 날짜를 기준으로 미션 시작일부터 오늘까지의 경과 일수(주말 제외)를 계산한다.
	public static int openedDaySequenceExcludeWeekend(
		LocalDate startDate,
		LocalDate endDate,
		LocalDate today
	) {

		log.info("openedDaySequenceExcludeWeekend 호출 startDate={}, endDate={}, today={}",
			startDate, endDate, today);

		if (today == null) {
			throw BaseException.from(MissionErrorCode.TODAY_DATE_CANNOT_BE_NULL);
		}

		// 1) 미션 종료일 이후라면 종료일까지만 계산하여 결과값을 고정한다.
		// 예를 들어, 미션 기간이 1월5일부터 1월 14일까지인데, 오늘이 1월 20일이라면 1월 14일까지의 평일 일수까지만 계산
		LocalDate effectiveEnd = today.isAfter(endDate) ? endDate : today;

		// 2) 미션 시작 전이면 진행 일수는 0이다.
		if (effectiveEnd.isBefore(startDate)) {
			log.info("오늘 날짜가 미션 시작일 이전이므로 진행 일수는 0으로 처리합니다. startDate={}, effectiveEnd={}",
				startDate, effectiveEnd);
			return 0;
		}

		// 3) 전체 일수 계산 (시작일 포함을 위해 +1)
		long totalDaysInclusive = ChronoUnit.DAYS.between(startDate, effectiveEnd) + 1;
		log.info("전체 일수(시작일 포함) totalDaysInclusive={}", totalDaysInclusive);

		// 4) 7일(1주일) 단위로 묶음 계산
		long fullWeeks = totalDaysInclusive / 7;
		long remainder = totalDaysInclusive % 7;

		// 5) 완전한 주(7일)는 무조건 평일이 5일이므로 바로 곱셈 처리
		long weekdays = fullWeeks * 5;

		// 6) 남은 날짜(remainder)에 대해 루프를 돌며 평일 여부 확인
		LocalDate cursor = startDate.plusDays(fullWeeks * 7);

		for (int i = 0; i < remainder; i++) {
			DayOfWeek dow = cursor.getDayOfWeek();

			if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
				weekdays++;
			}
			cursor = cursor.plusDays(1);
		}

		log.info("평일 일수 weekdays={}", weekdays);
		log.info("openedDaySequenceExcludeWeekend 호출 끝");
		return (int)weekdays;

	}
}
