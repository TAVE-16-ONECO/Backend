package com.oneco.backend.StudyRecord.infrastructure.persistence;

import static com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult.*;

import java.util.List;

import org.springframework.stereotype.Component;

import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardDailyContentReadPort;
import com.oneco.backend.dailycontent.domain.dailycontent.DailyContent;
import com.oneco.backend.dailycontent.domain.dailycontent.DaySequence;
import com.oneco.backend.dailycontent.infrastructure.persistence.DailyContentJpaRepository;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.global.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HomeDashboardDailyContentReadAdapter implements HomeDashboardDailyContentReadPort {

	private final DailyContentJpaRepository dailyContentJpaRepository;

	@Override
	public DailyContentResult findByCategoryIdAndDaySequence(Long categoryId, int elapsedDays) {

		DailyContent dailyContent = dailyContentJpaRepository.findByCategoryIdAndDaySequence(
				categoryId,
				new DaySequence(elapsedDays)
			).orElseThrow(() -> BaseException.from(StudyErrorCode.DAILY_CONTENT_NOT_FOUND));

		return new DailyContentResult(dailyContent.getId(), dailyContent.getKeyword().getValue());
	}

	@Override
	public List<DailyContentResult> findAllByCategoryIdOrderByDaySequence(Long categoryId) {
		// daySequence 오름차순으로 전체 조회
		return dailyContentJpaRepository.findAllByCategoryId_ValueOrderByDaySequence(categoryId)
			.stream()
			.map(dc -> new DailyContentResult(dc.getId(), dc.getKeyword().getValue()))
			.toList();
	}
}
