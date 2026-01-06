package com.oneco.backend.StudyRecord.infrastructure.persistence;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.port.dto.ActiveMissionSnapshot;
import com.oneco.backend.StudyRecord.application.port.out.MissionQueryPort;
import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.domain.mission.MissionStatus;
import com.oneco.backend.mission.infrastructure.MissionJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class MissionQueryAdapter implements MissionQueryPort {
	private final MissionJpaRepository missionJpaRepository;
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	/**
	 * 활성 미션 조회
	 * - memberId: 회원 ID
	 * - categoryId: 카테고리 ID
	 *
	 * @return 활성 미션 스냅샷 또는 빈 Optional
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<ActiveMissionSnapshot> findActiveMission(Long memberId, Long categoryId) {
		List<Mission> m = missionJpaRepository.findLatestActive(
			memberId,
			categoryId,
			MissionStatus.IN_PROGRESS
		);
		// 2) 없으면 empty
		if (m.isEmpty()) {
			return Optional.empty();
		}

		Mission mission = m.get(0);

		// 3) openedDaySequence 계산
		LocalDate today = LocalDate.now(KST);
		int openedDaySequence = mission.getCurrentOpenedDaySequence(today);

		ActiveMissionSnapshot snapshot = new ActiveMissionSnapshot(
			mission.getId(),
			mission.getCategoryId().getValue(),
			mission.isActiveForStudy(),      // status == IN_PROGRESS
			openedDaySequence
		);

		return Optional.of(snapshot);

	}

}

