package com.oneco.backend.StudyRecord.application.port.out;

import java.util.List;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult.DailyContentResult;
import com.oneco.backend.StudyRecord.application.port.dto.result.HomeKeywordResult;

public interface HomeDashboardDailyContentReadPort {

	// 카테고리 ID와 학습 일차로 DailyContent 조회
	DailyContentResult findByCategoryIdAndDaySequence(Long categoryId, int daySequence);

	// 카테고리의 모든 DailyContent 조회 (daySequence 오름차순)
	List<DailyContentResult> findAllByCategoryIdOrderByDaySequence(Long categoryId);

	// 회원 ID와 DailyContent ID로 HomeKeyword 조회
	HomeKeywordResult findKeywordByDailyContentId(Long dailyContentId);
}
