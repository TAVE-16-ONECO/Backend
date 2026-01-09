package com.oneco.backend.StudyRecord.infrastructure.persistence;

import static com.oneco.backend.StudyRecord.application.port.dto.result.HomeDashboardResult.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.oneco.backend.StudyRecord.application.port.dto.result.HomeActiveMissionsResult;
import com.oneco.backend.StudyRecord.application.port.out.HomeDashboardMissionReadPort;
import com.oneco.backend.mission.domain.mission.MissionId;
import com.oneco.backend.mission.domain.mission.MissionStatus;
import com.oneco.backend.mission.infrastructure.MissionJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HomeDashboardMissionReadAdapter implements HomeDashboardMissionReadPort {

	private final MissionJpaRepository repository;

	// 가장 최신의 활성 미션 조회
	@Override
	public Optional<MissionResult> findLatestActiveMission(Long memberId) {
		return repository.findTop1LatestActive(
			memberId,
			MissionStatus.IN_PROGRESS,
			PageRequest.of(0, 1)
		).stream().findFirst().map(mission -> MissionResult.of(
			mission.getId(),
			mission.getCategoryId().getValue(),
			mission.getPeriod().getStartDate(),
			mission.getPeriod().getEndDate()
		));
	}

	// 활성미션이면서, 특정 ID인 미션 조회
	@Override
	public Optional<MissionResult> findActiveMissionById(Long memberId, Long missionId) {
		return repository.findActiveByIdAndMember(
			memberId,
			missionId,
			MissionStatus.IN_PROGRESS
		).map(mission -> MissionResult.of(
			mission.getId(),
			mission.getCategoryId().getValue(),
			mission.getPeriod().getStartDate(),
			mission.getPeriod().getEndDate()
		));
	}

	@Override
	public List<MissionId> findActiveMissionsByMemberId(Long memberId) {
		return repository.findLatestActive(
				memberId,
				MissionStatus.IN_PROGRESS
			).stream()
			.map(mission -> MissionId.of(mission.getId()))
			.toList();
	}
}
