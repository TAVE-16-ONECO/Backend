package com.oneco.backend.mission.domain.judge.dto;

// 미션 실패 판단 결과 DTO
public record MissionFailureJudgement(
	boolean failed,
	String reason,
	int allowedWrongAnswers,
	int remainingWrongAnswers
) {
	// 미션 실패
	public static MissionFailureJudgement fail(String reason, int allowedWrongAnswers, int remainingWrongAnswers) {
		return new MissionFailureJudgement(true, reason, allowedWrongAnswers, remainingWrongAnswers);
	}

	// 미션 성공
	public static MissionFailureJudgement pass(int allowedWrongAnswers, int remainingWrongAnswers) {
		return new MissionFailureJudgement(false, "PASS", allowedWrongAnswers, remainingWrongAnswers);
	}
}
