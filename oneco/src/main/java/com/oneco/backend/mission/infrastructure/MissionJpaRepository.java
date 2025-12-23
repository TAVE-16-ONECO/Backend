package com.oneco.backend.mission.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneco.backend.mission.domain.mission.Mission;

public interface MissionJpaRepository extends JpaRepository<Mission, Long> {

}
