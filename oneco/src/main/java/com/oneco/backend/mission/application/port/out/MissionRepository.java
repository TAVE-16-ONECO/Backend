package com.oneco.backend.mission.application.port.out;

import java.util.Optional;

import com.oneco.backend.mission.domain.Mission;

public interface MissionRepository {
	Mission save(Mission mission);
	Optional<Mission> findById(Long id);
}
