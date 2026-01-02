package com.oneco.backend.mission.domain.policy;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;

@Component
public class MissionSchedulePolicy {

	// 주말(토,일)을 제외한 학습일수를 기준으로 미션 종료일 계산
	public LocalDate calculateDueDate(LocalDate startDate, int missionDays) {

		if (missionDays <= 0) {
			throw BaseException.from(
				MissionErrorCode.INVALID_MISSION_SCHEDULED,
				"미션에 할당된 카테고리의 학습일수는 1일 이상이어야합니다. missionDays: " + missionDays);
		}

		LocalDate first = adjustToWeekday(startDate); // 시작일을 주말 피해서 조정

		int remaining = missionDays - 1; // 첫날 제외한 남은 학습일수
		int fullWeeks = remaining / 5; // 남은 학습일수로 만들 수 있는 온전한 주 수
		int extraDays = remaining % 5; // 온전한 주 수를 제외한 나머지 학습일수

		LocalDate date = first.plusWeeks(fullWeeks); // 온전한 주 수만큼 더하기

		int dow = date.getDayOfWeek().getValue(); // Monday=1 ... Sunday=7
		// extraDays를 더했을 때 주말을 넘기면 +2(토,일) 스킵
		if (dow + extraDays > 5) {
			date = date.plusDays(extraDays + 2L);
		} else {
			date = date.plusDays(extraDays);
		}

		return date;

	}

	// 주말을 피해서 조정하는 메서드 -> 주말이면 다음 월요일로 조정한다.
	private LocalDate adjustToWeekday(LocalDate date) {
		DayOfWeek d = date.getDayOfWeek(); // 요일 가져오기
		if (d == DayOfWeek.SATURDAY) {
			return date.plusDays(2); // 토요일이면 2일을 더한다.
		}
		if (d == DayOfWeek.SUNDAY) {
			return date.plusDays(1); // 일요일이면 1일을 더한다.
		}
		return date; // 평일이면 그대로 반환
	}
}
