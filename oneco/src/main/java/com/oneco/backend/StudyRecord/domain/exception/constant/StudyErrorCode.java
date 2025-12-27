package com.oneco.backend.StudyRecord.domain.exception.constant;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.constant.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudyErrorCode implements ErrorCode {

	STUDY_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY_404", "학습 기록을 찾을 수 없습니다."),
	STUDY_RECORD_FORBIDDEN(HttpStatus.FORBIDDEN, "STUDY_403", "해당 학습 기록에 접근할 수 없습니다."),

	// 상태 전이가 불가능한 상황들(예: 아직 퀴즈 시작도 안 했는데 제출, 이미 종료인데 제출 등)
	INVALID_STUDY_STATUS(HttpStatus.CONFLICT, "STUDY_409_1", "현재 상태에서는 요청을 처리할 수 없습니다."),
	QUIZ_ALREADY_STARTED(HttpStatus.CONFLICT, "STUDY_409_2", "이미 퀴즈가 시작되었습니다."),
	QUIZ_ATTEMPT_EXCEEDED(HttpStatus.CONFLICT, "STUDY_409_3", "퀴즈 재시도 기회를 모두 사용했습니다."),
	QUIZ_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "STUDY_409_4", "해당 시도는 이미 제출되었습니다."),

	// 입력 검증
	INVALID_QUIZ_SUBMISSION(HttpStatus.BAD_REQUEST, "STUDY_400_1", "제출 데이터가 올바르지 않습니다."),
	QUIZ_ID_MISMATCH(HttpStatus.BAD_REQUEST, "STUDY_400_2", "제출한 문제 목록이 출제된 문제와 다릅니다."),
	INVALID_OPTION_INDEX(HttpStatus.BAD_REQUEST, "STUDY_400_3", "선택한 보기 인덱스가 올바르지 않습니다."),
	MISSION_ID_INVALID(HttpStatus.BAD_REQUEST, "STUDY_400_4", "미션 ID는 양수여야 합니다."),
	CATEGORY_ID_INVALID(HttpStatus.BAD_REQUEST, "STUDY_400_5", "카테고리 ID는 양수여야 합니다."),
	MEMBER_ID_INVALID(HttpStatus.BAD_REQUEST, "STUDY_400_6", "회원 ID는 양수여야 합니다."),
	DAILY_CONTENT_ID_INVALID(HttpStatus.BAD_REQUEST, "STUDY_400_7", "오늘의 콘텐츠 ID는 양수여야 합니다."),

	// 외부/연관 데이터 미존재,
	DAILY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_404", "오늘의 콘텐츠를 찾을 수 없습니다."),
	QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "QUIZ_404", "퀴즈 정보를 찾을 수 없습니다.");
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}