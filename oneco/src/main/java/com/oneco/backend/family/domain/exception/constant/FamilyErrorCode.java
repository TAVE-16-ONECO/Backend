package com.oneco.backend.family.domain.exception.constant;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FamilyErrorCode implements ErrorCode {

	MEMBER_ID_INVALID(
		HttpStatus.BAD_REQUEST,
		"회원 ID가 유효하지 않습니다.",
		"FAMILY_ERROR_400_MEMBER_ID_INVALID"
	),

	// === Family Relation Errors ===
	INVALID_FAMILY_RELATION_ID(
		HttpStatus.BAD_REQUEST,
		"가족 관계 ID가 유효하지 않습니다.",
		"FAMILY_ERROR_400_INVALID_FAMILY_RELATION_ID"
	),

	FAMILY_RELATION_INVALID_SAME_MEMBER(
		HttpStatus.BAD_REQUEST,
		"부모와 자식은 동일한 멤버일 수 없습니다.",
		"FAMILY_ERROR_400_FAMILY_RELATION_INVALID_SAME_MEMBER"
	),

	FAMILY_RELATION_DISCONNECT_FORBIDDEN(
		HttpStatus.UNAUTHORIZED,
		"가족 관계 해제는 본인만 가능합니다.",
		"FAMILY_ERROR_401_FAMILY_RELATION_DISCONNECT_FORBIDDEN"
	),

	FAMILY_RELATION_ALREADY_DISCONNECTED(
		HttpStatus.BAD_REQUEST,
		"이미 해제된 가족 관계입니다.",
		"FAMILY_ERROR_400_FAMILY_RELATION_ALREADY_DISCONNECTED"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;

}
