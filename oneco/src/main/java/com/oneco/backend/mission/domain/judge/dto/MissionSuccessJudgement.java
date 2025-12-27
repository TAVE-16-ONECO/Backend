package com.oneco.backend.mission.domain.judge.dto;

// 미션 성공 판단 결과 DTO
public record MissionSuccessJudgement(
	boolean success,
	String reason,
	double accuracy
) {
	public static MissionSuccessJudgement success(double accuracy) {
		return new MissionSuccessJudgement(true, "SUCCESS", accuracy);
	}

	public static MissionSuccessJudgement fail(String reason, double accuracy) {
		return new MissionSuccessJudgement(false, reason, accuracy);
	}
}
