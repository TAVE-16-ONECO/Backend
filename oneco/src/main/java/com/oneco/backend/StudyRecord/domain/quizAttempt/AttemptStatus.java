package com.oneco.backend.StudyRecord.domain.quizAttempt;

public enum AttemptStatus {
	IN_PROGRESS, // 시작만 하고 아직 제출 안 함
	SUBMITTED,   // 제출 완료(채점까지 끝났다면 여기에 포함)
	EXPIRED      // 시작했지만 TTL/시간 지나서 무효 (추후)
}
