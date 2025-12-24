package com.oneco.backend.family.domain.exception.constant;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FamilyErrorCode implements ErrorCode {

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
	),

	FAMILY_REQUIRED_VALUE_MISSING(
		HttpStatus.BAD_REQUEST,
		"가족 필수 값이 누락되었습니다.",
		"FAMILY_ERROR_400_REQUIRED_VALUE_MISSING"
	),

	FAMILY_MEMBER_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"회원 정보를 찾을 수 없습니다.",
		"FAMILY_ERROR_404_FAMILY_MEMBER_NOT_FOUND"
	),

	FAMILY_RELATION_ALREADY_EXISTS(
		HttpStatus.BAD_REQUEST,
		"이미 존재하는 가족 관계입니다.",
		"FAMILY_ERROR_400_FAMILY_RELATION_ALREADY_EXISTS"
	),

	FAMILY_RELATION_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"가족 관계를 찾을 수 없습니다.",
		"FAMILY_ERROR_404_FAMILY_RELATION_NOT_FOUND"
	),

	FAMILY_INVITATION_CODE_INVALID(
		HttpStatus.BAD_REQUEST,
		"가족 초대 코드가 유효하지 않습니다.",
		"FAMILY_ERROR_400_FAMILY_INVITATION_CODE_INVALID"
	),
	FAMILY_RELATION_CONNECT_INVALID(
		HttpStatus.BAD_REQUEST,
		"가족 연결 요청이 유효하지 않습니다.",
		"FAMILY_ERROR_400_FAMILY_RELATION_CONNECT_INVALID"
	),
	FAMILY_RELATION_PARENT_CHILD_LIMIT_EXCEEDED(
		HttpStatus.BAD_REQUEST,
		"자녀 최대 연결 수를 초과하였습니다.",
		"FAMILY_ERROR_400_FAMILY_RELATION_PARENT_CHILD_LIMIT_EXCEEDED"
	),
	FAMILY_RELATION_CHILD_PARENT_LIMIT_EXCEEDED(
		HttpStatus.BAD_REQUEST,
		"부모 최대 연결 수를 초과하였습니다.",
		"FAMILY_ERROR_400_FAMILY_RELATION_CHILD_PARENT_LIMIT_EXCEEDED"
	);



	private final HttpStatus httpStatus;
	private final String message;
	private final String code;

}
