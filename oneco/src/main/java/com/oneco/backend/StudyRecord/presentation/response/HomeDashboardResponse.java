package com.oneco.backend.StudyRecord.presentation.response;

// 반환값:
// [Mission] mission_id, start_date, end_date,
// [Category] category_id, category_title
// [DailyContent] daily_content_id, content_keyword
// [캘린더 뷰] 리스트 크기: DailyContent 수
// date list {
//    date: yyyy-MM-dd, // 날짜
//    dailyContentId // 해당 날짜의 일일 콘텐츠 ID (없으면 null)
//    studyStatus: [NOT_AVAILABLE(회색조개), IN_PROGRESS(하늘색조개), COMPLETED(파란색 조개)]
// }

import java.time.LocalDate;
import java.util.List;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult;

public record HomeDashboardResponse(
	// [Mission]
	Long missionId,
	String rewardTitle,
	LocalDate startDate,
	LocalDate endDate,

	// [Category]
	CategoryView category,

	// [DailyContent] (홈에서 대표로 내려줄 1건)
	DailyContentView dailyContent,

	// [캘린더 뷰] (리스트 크기: DailyContent 수)
	List<CalendarDateView> dateList
) {

	public static HomeDashboardResponse from(HomeDashboardResult result) {
		return new HomeDashboardResponse(
			// [Mission]
			result.missionResult().missionId(),
			result.missionResult().rewardTitle(),
			result.missionResult().startDate(),
			result.missionResult().endDate(),

			// [Category]
			new CategoryView(
				result.category().categoryId(),
				result.category().categoryTitle()
			),

			// [DailyContent]
			new DailyContentView(
				result.dailyContent().dailyContentId(),
				result.dailyContent().contentKeyword()
			),

			// [캘린더 뷰]
			result.dateList().stream()
				.map(dateResult -> new CalendarDateView(
					dateResult.date(),
					dateResult.dailyContentId(),
					switch (dateResult.studyStatus()) {
						case NOT_AVAILABLE -> StudyStatus.NOT_AVAILABLE;
						case IN_PROGRESS -> StudyStatus.IN_PROGRESS;
						case COMPLETED -> StudyStatus.COMPLETED;
					}
				))
				.toList()
		);
	}

	public enum StudyStatus {
		NOT_AVAILABLE, // 회색 조개
		IN_PROGRESS,   // 하늘색 조개
		COMPLETED      // 파란색 조개
	}

	public record CategoryView(
		Long categoryId,
		String categoryTitle
	) {
	}

	public record DailyContentView(
		Long dailyContentId,
		String contentKeyword
	) {
	}

	public record CalendarDateView(
		LocalDate date,          // yyyy-MM-dd
		Long dailyContentId,     // 없으면 null
		StudyStatus studyStatus  // NOT_AVAILABLE / IN_PROGRESS / COMPLETED
	) {
	}
}
