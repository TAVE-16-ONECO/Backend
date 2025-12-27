package com.oneco.backend.mission.infrastructure;

import org.springframework.stereotype.Component;

import com.oneco.backend.category.domain.category.MissionDays;
import com.oneco.backend.content.domain.dailycontent.CategoryId;
import com.oneco.backend.member.domain.MemberRepository;
import com.oneco.backend.mission.application.port.out.CategoryLookupPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryLookupJpaAdapter implements CategoryLookupPort {
	//private final CategoryRepository repository;

	@Override
	public MissionDays getDefaultMissionDays(CategoryId categoryId) {
		// TODO: CategoryRepository를 통해 기본 MissionDays를 조회하는 로직 구현
		// return repository.findByCategoryId(categoryId).getDefaultMissionDays();
		return MissionDays.of(10); // 임시로 10일 반환
	}
}
