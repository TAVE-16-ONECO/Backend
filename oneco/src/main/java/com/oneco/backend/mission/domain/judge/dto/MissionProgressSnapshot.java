package com.oneco.backend.mission.domain.judge.dto;

// 사용자가 학습하고 나서 미션 통과 판단에 필요한 스냅샷 정보입니다.
// MissionJudgementService 에서 사용됩니다.
public record MissionProgressSnapshot(
	int totalKeywords,           // 카테고리별 키워드 개수
	int learnedKeywords,         // 학습 완료한 키워드 수
	int requiredQuizCount,       // 3 * N
	int solvedQuizCount,         // 실제로 푼 퀴즈 수(카테고리 내)
	int correctQuizCount         // 그 중 정답 수
) {
	public double accuracy() {
		// 정답률 계산
		if (solvedQuizCount == 0) {
			return 0.0;
	}
		// 정답률 = 맞춘 퀴즈 수 / 푼 퀴즈
		return (double) correctQuizCount / solvedQuizCount;
	}
}
