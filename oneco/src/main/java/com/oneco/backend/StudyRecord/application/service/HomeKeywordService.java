package com.oneco.backend.StudyRecord.application.service;

import org.springframework.stereotype.Service;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeKeywordResult;
import com.oneco.backend.StudyRecord.application.port.in.HomeKeywordUseCase;
import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardDailyContentReadPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeKeywordService implements HomeKeywordUseCase {

	private final HomeDashboardDailyContentReadPort dailyContentReadPort;

	@Override
	public HomeKeywordResult getKeyword(Long dailyContentId) {
		return dailyContentReadPort.findKeywordByDailyContentId(dailyContentId);
	}
}
