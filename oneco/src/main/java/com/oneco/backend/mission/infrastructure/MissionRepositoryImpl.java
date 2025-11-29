package com.oneco.backend.mission.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.oneco.backend.mission.domain.Mission;
import com.oneco.backend.mission.application.port.out.MissionRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepository {

	private final MissionJpaRepository missionJpaRepository;

	@Override
	public Mission save(Mission mission) {
		return missionJpaRepository.save(mission);
	}

	@Override
	public Optional<Mission> findById(Long id) {
		return missionJpaRepository.findById(id);
	}

	// 추가적인 쿼리 메서드 구현 가능
}
