package com.oneco.backend.StudyRecord.application.port.dto.result;

import java.time.LocalDate;
import java.util.List;

public record HomeDashboardResult(
	// [Mission]
	MissionResult missionResult,

	// [경과 일수, 진행률]
	long elapsedDays,
	long progressPercentage,

	// [Category]
	CategoryResult category,

	// [DailyContent] (홈에서 대표로 내려줄 1건)
	DailyContentResult dailyContent,

	// [캘린더 뷰]
	List<CalendarDateResult> dateList
) {

	public enum StudyStatusResult {
		NOT_AVAILABLE, // 회색 조개(아직 학습 불가)
		IN_PROGRESS,   // 하늘색 조개(학습 중)
		COMPLETED      // 파란색 조개(학습 완료)
	}

	public record MissionResult(
		Long missionId,
		Long childId,
		Long categoryId,
		String rewardTitle,
		LocalDate startDate,
		LocalDate endDate
	) {
		public static MissionResult of(
			Long missionId,
			Long childId,
			Long categoryId,
			String rewardTitle,
			LocalDate startDate,
			LocalDate endDate
		) {
			return new MissionResult(missionId, childId, categoryId, rewardTitle, startDate, endDate);
		}
	}

	public record CategoryResult(
		Long categoryId,
		String categoryTitle
	) {
	}

	public record DailyContentResult(
		Long dailyContentId,
		String contentKeyword
	) {
		public static DailyContentResult of(Long dailyContentId, String contentKeyword) {
			return new DailyContentResult(dailyContentId, contentKeyword);
		}
	}

	public record CalendarDateResult(
		LocalDate date,      // yyyy-MM-dd
		Long dailyContentId, // 없으면 null
		StudyStatusResult studyStatus
	) {
		public static CalendarDateResult of(
			LocalDate date,
			Long dailyContentId,
			StudyStatusResult studyStatus
		) {
			return new CalendarDateResult(date, dailyContentId, studyStatus);
		}
	}
}
