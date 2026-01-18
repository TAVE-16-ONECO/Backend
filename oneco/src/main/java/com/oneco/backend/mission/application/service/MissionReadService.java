package com.oneco.backend.mission.application.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.global.response.CursorResponse;
import com.oneco.backend.member.domain.Member;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.member.infrastructure.persistence.MemberJpaRepository;
import com.oneco.backend.mission.application.port.out.CategoryLookupPort;
import com.oneco.backend.mission.application.port.out.FamilyRelationLookupPort;
import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.domain.mission.MissionStatus;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.presentation.response.MissionCountResponse;
import com.oneco.backend.mission.presentation.response.MissionExistsResponse;
import com.oneco.backend.mission.presentation.response.MissionDetailResponse;
import com.oneco.backend.mission.presentation.response.MissionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정
public class MissionReadService {

	private final MissionPersistencePort missionPort;
	private final FamilyRelationLookupPort familyRelationPort;
	private final CategoryLookupPort categoryLookupPort;
	private final MemberJpaRepository memberJpaRepository;

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
		String missionTitle = categoryLookupPort.getCategoryTitle(mission.getCategoryId()).getValue();

		// rewardTitle이 null일 수 있으므로 조건부로 처리
		String rewardTitle = mission.getReward() == null ? null : mission.getReward().getTitle();
		return new MissionResponse(
			mission.getId(),
			missionTitle,
			rewardTitle,
			mission.getStatus().name() // Enum의 name() 메서드를 사용하여 문자열로 변환
		);
	}

	public MissionCountResponse countMyMissions(MemberId memberId) {
		// memberId로 가족 관계 조회
		FamilyRelationId relationId = familyRelationPort.findRelationIdByMemberId(memberId);

		// 전체 미션 개수, 진행중인 미션 개수, 종료된 미션 개수 조회
		long totalCount = missionPort.countMissionsByFamilyRelation(relationId);

		// 진행중인 상태와 종료된 상태의 MissionStatus 리스트 생성
		List<MissionStatus> inProgressStatuses = Arrays.stream(MissionStatus.values())
			.filter(MissionStatus::isInProgress)
			.toList();
		List<MissionStatus> finishedStatuses = Arrays.stream(MissionStatus.values())
			.filter(MissionStatus::isFinished)
			.toList();

		long inProgressCount = missionPort.countMissionsByFamilyRelationAndStatuses(relationId, inProgressStatuses);
		long finishedCount = missionPort.countMissionsByFamilyRelationAndStatuses(relationId, finishedStatuses);

		return MissionCountResponse.of(totalCount, inProgressCount, finishedCount);
	}

	// 회원의 진행중인 미션이 있는지 확인하는 메서드
	public MissionExistsResponse existsInProgressMission(MemberId memberId) {
		// memberId로 가족 관계 조회
		FamilyRelationId relationId = familyRelationPort.findRelationIdByMemberId(memberId);

		// 진행중인 상태의 MissionStatus 리스트 생성
		List<MissionStatus> inProgressStatuses = Arrays.stream(MissionStatus.values())
			.filter(MissionStatus::isInProgress)
			.toList();

		// 진행중인 미션 존재 여부 확인
		boolean exists = missionPort.existsByFamilyRelationAndInProgressStatus(relationId, inProgressStatuses);
		return new MissionExistsResponse(exists);
	}

	public MissionDetailResponse getMissionDetailById(MemberId memberId, Long missionId) {
		Mission mission = missionPort.findById(missionId);
		FamilyRelationId relationId = familyRelationPort.findRelationIdByMemberId(memberId);

		if (!mission.getFamilyRelationId().equals(relationId)) {
			throw BaseException.from(MissionErrorCode.INVALID_FAMILY_RELATION_MEMBERS);
		}

		String categoryTitle = categoryLookupPort.getCategoryTitle(mission.getCategoryId()).getValue();
		String rewardTitle = mission.getReward() == null ? null : mission.getReward().getTitle();

		// 닉네임 조회
		Member member = memberJpaRepository.findById(memberId.getValue())
			.orElseThrow(() -> BaseException.from(MissionErrorCode.MEMBER_NOT_FOUND));
		return MissionDetailResponse.of(
			mission.getId(),
			categoryTitle,
			rewardTitle,

			mission.getPeriod().getStartDate(),
			mission.getPeriod().getEndDate(),
			mission.getStatus().name(),
			memberId.getValue(),
			mission.getRecipientId().getValue(),
			mission.getRequesterId().getValue(),
			member.getNickname()
		);
	}
}
