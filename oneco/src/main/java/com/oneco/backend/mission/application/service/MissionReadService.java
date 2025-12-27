package com.oneco.backend.mission.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.global.response.CursorResponse;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.mission.application.port.out.FamilyRelationLookupPort;
import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.presentation.response.MissionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정
public class MissionReadService {

	private final MissionPersistencePort missionPort;
	private final FamilyRelationLookupPort familyRelationPort;

	// 현재 진행중인 미션을 조회한다.
	// 순서는 미션 생성 시점 기준으로 페이징 처리한다.(createdAt)
	// todo: createdAt 동시성 문제 고려
	public CursorResponse<MissionResponse> getInProgressMissions(MemberId memberId, Long lastId, Integer size) {
		// memberId로 가족 관계 조회
		FamilyRelationId relationId = familyRelationPort.findRelationIdByMemberId(memberId);
		Long cursor = lastId == null ? Long.MAX_VALUE : lastId; // 커서 초기화 (마지막 ID가 없으면 최대값으로 설정) -> 가장 최신 미션부터 조회
		int pageSize = (size == null || size <= 0) ? 5 : size;
		// 가족 관계와 진행중인 상태의 미션 조회
		List<Mission> missions = missionPort.findByFamilyRelationAndInProgressStatus(relationId, cursor, pageSize);
		return toCursorResponse(missions, pageSize);
	}

	// 종료된 미션 조회
	// 순서는 미션 생성 시점 기준으로 페이징 처리한다.(createdAt)
	// todo: createdAt 동시성 문제 고려(나중에 개선한다.)
	public CursorResponse<MissionResponse> getFinishedMissions(MemberId memberId, Long lastId, Integer size) {
		// memberId로 가족 관계 조회
		FamilyRelationId relationId = familyRelationPort.findRelationIdByMemberId(memberId);
		Long cursor = lastId == null ? Long.MAX_VALUE : lastId; // 커서 초기화 (마지막 ID가 없으면 최대값으로 설정) -> 가장 최신 미션부터 조회
		int pageSize = (size == null || size <= 0) ? 5 : size; // 기본 페이지 크기 설정
		List<Mission> missions = missionPort.findByFamilyRelationAndFinishedStatus(relationId, cursor, pageSize);
		return toCursorResponse(missions, pageSize);
	}

	private CursorResponse<MissionResponse> toCursorResponse(List<Mission> missions, int pageSize) {
		// Mission을 MissionResponse로 변환
		List<MissionResponse> content = missions.stream()
			.map(this::toMissionResponse)
			.collect(Collectors.toList());
		// 다음 커서 설정: 마지막 미션의 ID, 없으면 null
		Long nextCursor = missions.isEmpty() ? null : missions.get(missions.size() - 1).getId();
		boolean hasNext = missions.size() == pageSize;
		return CursorResponse.of(content, nextCursor, hasNext);
	}

	private MissionResponse toMissionResponse(Mission mission) {
		// 임시로 missionTitle을 categoryId 값으로 설정
		// todo: 나중에 카테고리 제목을 가져오는 로직으로 변경
		// todo: 카테고리 도메인에서 제목을 가져오는 로직이 필요하다.
		String missionTitle = String.valueOf(mission.getCategoryId().getValue());

		// rewardTitle이 null일 수 있으므로 조건부로 처리
		String rewardTitle = mission.getReward() == null ? null : mission.getReward().getTitle();
		return new MissionResponse(
			mission.getId(),
			missionTitle,
			rewardTitle,
			mission.getStatus().name() // Enum의 name() 메서드를 사용하여 문자열로 변환
		);
	}
}
