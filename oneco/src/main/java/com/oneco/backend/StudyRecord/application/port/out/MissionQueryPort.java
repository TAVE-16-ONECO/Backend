package com.oneco.backend.StudyRecord.application.port.out;

import java.util.Optional;

import com.oneco.backend.StudyRecord.application.port.dto.ActiveMissionSnapshot;

public interface MissionQueryPort {

	Optional<ActiveMissionSnapshot> findActiveMission(Long memberId, Long categoryId);
}
