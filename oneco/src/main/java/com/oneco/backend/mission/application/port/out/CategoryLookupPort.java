package com.oneco.backend.mission.application.port.out;

import com.oneco.backend.category.domain.category.CategoryId;
import com.oneco.backend.category.domain.category.MissionDays;


// 카테고리 도메인 조회 포트
public interface CategoryLookupPort {

	MissionDays getDefaultMissionDays(CategoryId categoryId);

}
