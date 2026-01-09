package com.oneco.backend.StudyRecord.application.port.out;

import static com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult.*;

import java.util.List;
import java.util.Optional;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeActiveMissionsResult;

public interface HomeDashboardMissionReadPort {
	// 회원의 최신 진행중인 미션 1건 조회
	Optional<MissionResult> findLatestActiveMission(Long memberId);

	// 특정 미션의 진행중인 미션 1건 조회
	Optional<MissionResult> findActiveMissionById(Long memberId, Long missionId);

	List<HomeActiveMissionsResult> findActiveMissionsByMemberId(Long memberId);
}
