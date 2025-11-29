package com.oneco.backend.mission.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.mission.application.dto.RequestMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;
import com.oneco.backend.mission.application.port.in.RequestMissionUseCase;
import com.oneco.backend.mission.domain.Mission;
import com.oneco.backend.mission.domain.MissionPeriod;
import com.oneco.backend.mission.application.port.out.MissionRepository;
import com.oneco.backend.mission.domain.Reward;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestMissionService implements RequestMissionUseCase {

	private final MissionRepository missionRepository;
	// private final MemberReader memberReader; // TODO: 멤버 도메인 완성 후 주입
	// private final FamilyRelationReader familyRelationReader; // TODO: 가족 관계 도메인 완성 후 주입

	@Override
	@Transactional
	public MissionResult request(RequestMissionCommand command) {

		// TODO: Member/FamilyRelation 도메인 완성되면 실제 검증 로직 추가
		// MemberInfo member = memberReader.getById(command.memberId());
		// FamilyRelationInfo relation = familyRelationReader.getById(command.familyRelationId());
		// if (!relation.isApproved() || !relation.isParent(member.id())) { ... }

		// 2. 도메인 값/ 엔티티 생성
		MissionPeriod period = MissionPeriod.of(command.startDate(), command.endDate());
		Reward reward = Reward.of(command.title(), command.title());

		Mission mission = Mission.create(
			command.familyRelationId(), // 지금은 id만 그대로 사용 TODO: 관계 검증 후 변경 예정 -> relation.id()
			period,
			reward
		);

		// 3. 저장
		missionRepository.save(mission);
		return new MissionResult(mission.getId(), mission.getStatus());
	}

}

