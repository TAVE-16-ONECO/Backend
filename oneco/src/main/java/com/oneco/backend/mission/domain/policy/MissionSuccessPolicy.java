package com.oneco.backend.mission.domain.policy;

import org.springframework.stereotype.Component;

import com.oneco.backend.mission.domain.judge.dto.MissionProgressSnapshot;
import com.oneco.backend.mission.domain.judge.dto.MissionSuccessJudgement;

@Component
public class MissionSuccessPolicy {
	// 미션 성공은 다음 조건을 모두 만족해야한다.
	// 1. 키워드 N개를 모두 학습하였는가?
	// 2. 퀴즈 3*N개를 모두 풀었는가
	// 3. 정답률이 80% 이상인가?

	protected static final int QUIZZES_PER_KEYWORD = 3; // 키워드 당 퀴즈 수
	protected static final double MIN_SUCCESS_RATE = 0.8; // 최소 성공률 (80%)

	public MissionSuccessJudgement isMissionSuccessful(MissionProgressSnapshot snapshot) {
		int requiredQuizCount = QUIZZES_PER_KEYWORD * snapshot.totalKeywords(); // 키워드 당 문제 수 * 총 키워드 수

		// 1. 키워드 N개를 모두 학습하였는가?
		if (snapshot.learnedKeywords() < snapshot.totalKeywords()) {
			return MissionSuccessJudgement.fail("모든 키워드를 학습하지 않았습니다.", snapshot.accuracy());
		}
		// 2. 퀴즈 3*N개를 모두 풀었는가?
		if (snapshot.solvedQuizCount() < requiredQuizCount) {
			return MissionSuccessJudgement.fail("모든 퀴즈를 풀지 않았습니다.", snapshot.accuracy());
		}
		// 3. 정답률이 80% 이상인가?
		if (snapshot.accuracy() < MIN_SUCCESS_RATE) {
			return MissionSuccessJudgement.fail("정답률이 " + MIN_SUCCESS_RATE + "보다 낮습니다.", snapshot.accuracy());
		}

		return MissionSuccessJudgement.success(snapshot.accuracy());
	}
}
