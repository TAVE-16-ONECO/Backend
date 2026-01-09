package com.oneco.backend.StudyRecord.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeActiveMissionsResult;
import com.oneco.backend.StudyRecord.application.port.in.HomeActiveMissionsUseCase;
import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardMissionReadPort;
import com.oneco.backend.member.domain.MemberId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeActiveMissionsService implements HomeActiveMissionsUseCase {

	private final HomeDashboardMissionReadPort homeDashboardMissionReadPort;

	@Override
	public HomeActiveMissionsResult getActiveMissions(Long memberId) {
		MemberId.of(memberId);

		List<HomeActiveMissionsResult> activeMissions = homeDashboardMissionReadPort
			.findActiveMissionsByMemberId(memberId);

		Long missionsCount = (long)activeMissions.size();

		return activeMissions.stream()
			.findFirst()
			.orElseGet(() -> HomeActiveMissionsResult.of(missionsCount, List.of()));
	}
}
