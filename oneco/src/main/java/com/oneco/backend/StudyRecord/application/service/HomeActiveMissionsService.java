package com.oneco.backend.StudyRecord.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeActiveMissionsResult;
import com.oneco.backend.StudyRecord.application.port.in.HomeActiveMissionsUseCase;
import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardMissionReadPort;
import com.oneco.backend.mission.domain.mission.MissionId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeActiveMissionsService implements HomeActiveMissionsUseCase {

	private final HomeDashboardMissionReadPort homeDashboardMissionReadPort;

	@Override
	public HomeActiveMissionsResult getActiveMissions(Long memberId) {

		List<MissionId> activeMissions = homeDashboardMissionReadPort
			.findActiveMissionsByMemberId(memberId);

		Long missionsCount = (long)activeMissions.size();
		return HomeActiveMissionsResult.of(missionsCount, activeMissions);
	}
}
