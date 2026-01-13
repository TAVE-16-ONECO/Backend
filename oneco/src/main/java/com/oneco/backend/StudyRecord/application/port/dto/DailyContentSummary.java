package com.oneco.backend.StudyRecord.application.port.dto;

import java.util.List;

import com.oneco.backend.StudyRecord.application.dto.result.NewsItemSummary;

public record DailyContentSummary(
	Long dailyContentId,
	String title,
	String summary,
	List<NewsItemSummary> newsItemSummaryList
) {
	// 불변 리스트 생성
	// 생성자인데 ()가 없는 이유:
	// - 레코드의 컴팩트 생성자(compact constructor)라고 불리는 문법
	// - 필드 선언부에 이미 타입과 이름이 정의되어 있기 때문에
	//   생성자 매개변수로 다시 선언할 필요가 없음
	// - 따라서 매개변수 목록이 비어 있어도 컴파일러가 자동으로 필드에 접근할 수 있음
	// - 주로 필드 값에 대한 추가 검증이나 변환 로직을 작성할 때 사용
	// - 여기서는 newsItemSummaryList가 null인 경우 빈 리스트로 초기화하는 역할
	// - 즉, 별도의 매개변수 선언 없이도 필드에 접근하여 초기화 작업을 수행할 수 있음
	public DailyContentSummary {
		newsItemSummaryList = (newsItemSummaryList == null)
			? List.of()
			: List.copyOf(newsItemSummaryList);
	}
}
