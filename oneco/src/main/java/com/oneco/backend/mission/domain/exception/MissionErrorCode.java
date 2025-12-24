package com.oneco.backend.mission.domain.exception;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MissionErrorCode implements ErrorCode {

	INVALID_MISSION_TIME_ORDER
		(HttpStatus.BAD_REQUEST,
			"미션 시작시간은 종료시간보다 이전이어야 합니다.",
			"MISSION_ERROR_400_INVALID_MISSION_TIME_ORDER"
		),

	MISSION_TIME_CANNOT_BE_NULL(
		HttpStatus.BAD_REQUEST,
		"미션의 시작시간과 종료시간은 필수 항목입니다.",
		"MISSION_TIME_CANNOT_BE_NULL"
	),

	MISSION_REWARD_CANNOT_BE_BLANK(
		HttpStatus.BAD_REQUEST,
		"미션 생성 시 보상은 필수 항목입니다.",
		"MISSION_REWARD_CANNOT_BE_BLANK"
	),
	INVALID_UPDATE_MISSION_STATUS(
		HttpStatus.BAD_REQUEST,
		"잘못된 미션 상태 변경입니다.",
		"INVALID_UPDATE_MISSION_STATUS"
	),

	// === 미션 생성시 생성자 파라미터 검증 관련 ===
	FAMILY_RELATION_ID_CANNOT_BE_NULL(
		HttpStatus.BAD_REQUEST,
		"미션 생성은 가족을 선택해야합니다.",
		"FAMILY_RELATION_ID_CANNOT_BE_NULL"
	),

	MISSION_PERIOD_CANNOT_BE_NULL(
		HttpStatus.BAD_REQUEST,
		"미션 기간은 필수 항목입니다.",
		"MISSION_PERIOD_CANNOT_BE_NULL"
	),

	INVALID_MISSION_SCHEDULED(
		HttpStatus.BAD_REQUEST,
		"미션 스케줄이 유효하지 않습니다.",
		"MISSION_ERROR_400_INVALID_MISSION_SCHEDULED"

	),
	CATEGORY_ID_CANNOT_BE_NULL(
		HttpStatus.BAD_REQUEST,
		"미션 카테고리는 필수 항목입니다.",
		"MISSION_ERROR_400_CATEGORY_ID_CANNOT_BE_NULL"
	),

	MISSION_REQUIRED_VALUE_MISSING(
		HttpStatus.BAD_REQUEST,
		"미션 생성에 필요한 값이 누락되었습니다.",
		"MISSION_ERROR_400_MISSION_REQUIRED_VALUE_MISSING"
	),

	INVALID_MISSION_PERIOD(
		HttpStatus.BAD_REQUEST,
		"미션 기간이 유효하지 않습니다.",
		"MISSION_ERROR_400_INVALID_MISSION_PERIOD"
	),

	MEMBER_ID_CANNOT_BE_NULL(
		HttpStatus.BAD_REQUEST,
		"멤버 ID는 필수 항목입니다.",
		"MEMBER_ID_CANNOT_BE_NULL"
	),

	DUPLICATE_MISSION_FOR_FAMILY_CATEGORY(
		HttpStatus.CONFLICT,
		"같은 가족 관계에서 동일 카테고리 미션은 중복 생성할 수 없습니다.",
		"MISSION_ERROR_409_DUPLICATE_FAMILY_CATEGORY"
	),

	INVALID_FAMILY_RELATION_MEMBERS(
		HttpStatus.BAD_REQUEST,
		"요청자와 수신자가 해당 가족 관계에 속하지 않습니다.",
		"MISSION_ERROR_400_INVALID_FAMILY_MEMBERS"
	)
	;

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;

}
