package com.oneco.backend.mission.domain.exception;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MissionErrorCode implements ErrorCode {

	INVALID_MISSION_TIME_ORDER(HttpStatus.BAD_REQUEST, "미션 시작시간은 종료시간보다 이전이어야 합니다.",
		"MISSION_ERROR_400_INVALID_MISSION_TIME_ORDER"),
	MISSION_TIME_CANNOT_BE_NULL(HttpStatus.BAD_REQUEST, "미션의 시작시간과 종료시간은 필수 항목입니다.",
		"MISSION_TIME_CANNOT_BE_NULL"),
	FAMILY_RELATION_ID_CANNOT_BE_NULL(HttpStatus.BAD_REQUEST, "미션 생성은 가족을 선택해야합니다.",
		"FAMILY_RELATION_ID_CANNOT_BE_NULL"),
	MISSION_REWARD_CANNOT_BE_BLANK(HttpStatus.BAD_REQUEST, "미션 생성 시 보상은 필수 항목입니다.",
		"MISSION_REWARD_CANNOT_BE_BLANK"),
	INVALID_UPDATE_MISSION_STATUS(HttpStatus.BAD_REQUEST, "잘못된 미션 상태 변경입니다.",
		"INVALID_UPDATE_MISSION_STATUS"),
	INVALID_MISSION_ID(HttpStatus.BAD_REQUEST, "미션 ID가 유효하지 않습니다.", "MISSION_ERROR_401_INVALID_MISSION_ID");
	private final HttpStatus httpStatus;
	private final String message;
	private final String code;

}
