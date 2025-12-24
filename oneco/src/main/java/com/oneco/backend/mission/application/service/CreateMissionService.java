package com.oneco.backend.mission.application.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.category.domain.category.MissionDays;
import com.oneco.backend.content.domain.dailycontent.CategoryId;
import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.mission.application.dto.CreateMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;
import com.oneco.backend.mission.application.port.in.CreateMissionUseCase;
import com.oneco.backend.mission.application.port.out.FamilyRelationLookupPort;
import com.oneco.backend.mission.application.port.out.CategoryLookupPort;
import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.mission.Mission;
import com.oneco.backend.mission.domain.mission.MissionPeriod;
import com.oneco.backend.mission.domain.mission.Reward;
import com.oneco.backend.mission.domain.policy.MissionSchedulePolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMissionService implements CreateMissionUseCase {

	private final MissionPersistencePort missionPort;
	private final MissionSchedulePolicy missionSchedulePolicy;
	private final CategoryLookupPort categoryPort;
	private final FamilyRelationLookupPort familyRelationLookupPort;


	@Override
	@Transactional
	public MissionResult request(CreateMissionCommand command) {

		// requesterId 받아오기
		MemberId requesterId = MemberId.of(requireNonNull(command.requesterId(), "requester_id null 입니다."));

		// recipientId 받아오기
		MemberId recipientId = MemberId.of(requireNonNull(command.recipientId(), "recipient_id null 입니다."));

		// FamilyRelation 받아오기
		FamilyRelationId relationId = FamilyRelationId.of(requireNonNull(command.familyRelationId(), "FamilyRelationId가 null 입니다."));

		// requesterId와 recipientId가 서로 FamilyRelation에 속하는지 검증한다.
		if (!familyRelationLookupPort.isMembersOfRelation(relationId, requesterId, recipientId)) {
			throw BaseException.from(MissionErrorCode.INVALID_FAMILY_RELATION_MEMBERS);
		}

		// CategoryId 받아오기
		CategoryId categoryId = CategoryId.of(requireNonNull(command.categoryId(), "CategoryId가 null 입니다."));
		if (missionPort.existsByFamilyRelationAndCategory(relationId, categoryId)) {
			throw BaseException.from(
				MissionErrorCode.DUPLICATE_MISSION_FOR_FAMILY_CATEGORY,
				"이미 동일 가족 관계와 카테고리로 생성된 미션이 있습니다."
			);
		}

		// MissionPeriod 받아오기
		LocalDate startDate = requireNonNull(command.startDate(), "startDate가 null 입니다.");
		LocalDate endDate = requireNonNull(command.endDate(), "endDate가 null 입니다.");
		MissionPeriod period = MissionPeriod.of(startDate, endDate);
		validateMissionPeriod(period, categoryPort.getDefaultMissionDays(categoryId)); // period 검증(주말 제외)

		// Reward 받아오기 (nullable)
		String title = requireNonNull(command.title(), "title이 null 입니다.");
		Reward reward = Reward.of(title, command.message());

		// Mission 생성시 필요한 값: FamilyRelationId, CategoryId, Period, Reward(nullable)
		Mission mission = Mission.create(requesterId, recipientId, relationId, categoryId, period, reward);

		// 3. 저장
		missionPort.save(mission);
		return new MissionResult(mission.getId(), mission.getStatus());
	}

	private <T> T requireNonNull(T value, String msg) {
		if (value == null) {
			throw BaseException.from(MissionErrorCode.MISSION_REQUIRED_VALUE_MISSING, msg);
		}
		return value;
	}

	private void validateMissionPeriod(MissionPeriod period, MissionDays missionDays) {
		// CreateMissionCommand 에서 받은 period의 endDate가, missionDays에 맞게 계산된 endDate와 일치하는지 검증한다.
		LocalDate expectedEndDate = missionSchedulePolicy.calculateDueDate(period.getStartDate(), missionDays.getValue());
		if (!expectedEndDate.isEqual(period.getEndDate())) {
			throw BaseException.from(
				MissionErrorCode.INVALID_MISSION_PERIOD,
				"미션 endDate가 올바르지 않습니다. 기댓값: " + expectedEndDate + ", 실제값: " + period.getEndDate()
			);
		}
	}

}
