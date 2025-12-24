package com.oneco.backend.StudyRecord.domain.studyRecord;

public enum QuizProgressStatus {
	READY,           // 아직 시작 안 함
	IN_PROGRESS,     // 현재 시도 진행 중(제출 전)
	RETRY_AVAILABLE, // 1차 제출 FAIL → 2차 시작 가능
	PASSED,          // 1차 또는 2차에서 올패스(3/3)
	FAILED           // 2차까지 FAIL로 종료
}
