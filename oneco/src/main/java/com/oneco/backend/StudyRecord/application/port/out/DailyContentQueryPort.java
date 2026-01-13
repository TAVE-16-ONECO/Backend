package com.oneco.backend.StudyRecord.application.port.out;

import java.util.List;
import java.util.Map;

import com.oneco.backend.StudyRecord.application.dto.result.NewsItemSummary;
import com.oneco.backend.StudyRecord.application.dto.result.SubmitQuizSubmissionResult;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSnapshot;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSummary;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentWithQuizzesSnapshot;

/**
 * DailyContent 조회 전용 Port
 * - 다른 바운디드 컨텍스트(Content)로부터 필요한 정보만 스냅샷으로 가져옴
 */
public interface DailyContentQueryPort {

	DailyContentSnapshot loadDailyContentSnapshot(Long dailyContentId);

	/**
	 * READY 상태에서 퀴즈 도전하기 누르면:
	 * - DailyContent + Quizzes(정답은 제외한 뷰용 정보)를 함께 가져오고
	 * - quizIds도 여기서 만들어진다.
	 */
	DailyContentWithQuizzesSnapshot loadDailyContentWithQuizzes(Long dailyContentId);

	List<NewsItemSummary> loadNewsItemSummary(Long dailyContentId);

	Map<Long,DailyContentSummary> findDailyContentSummariesByIds(List<Long> dailyContentIds);


}
