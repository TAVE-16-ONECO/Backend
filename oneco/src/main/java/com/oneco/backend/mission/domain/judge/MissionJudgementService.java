package com.oneco.backend.mission.domain.judge;

import org.springframework.stereotype.Component;

import com.oneco.backend.mission.domain.judge.dto.MissionFailureJudgement;
import com.oneco.backend.mission.domain.judge.dto.MissionProgressSnapshot;
import com.oneco.backend.mission.domain.judge.dto.MissionSuccessJudgement;
import com.oneco.backend.mission.domain.policy.MissionFailurePolicy;
import com.oneco.backend.mission.domain.policy.MissionSuccessPolicy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MissionJudgementService {

	// ============================
	// 미션 성공 및 실패 판단 서비스
	// MissionProgressSnapshot을 받아 미션 성공/실패 여부를 판단합니다.
	// 미션 성공 정책과 실패 정책을 사용하여 판단 로직을 분리합니다.
	// ============================

	private final MissionSuccessPolicy missionSuccessPolicy;
	private final MissionFailurePolicy missionFailurePolicy;

	public MissionSuccessJudgement judgeSuccess(MissionProgressSnapshot snapshot) {
		return missionSuccessPolicy.isMissionSuccessful(snapshot);
	}

	public MissionFailureJudgement judgeFailure(MissionProgressSnapshot snapshot) {
		// 총 문항 수
		int totalQuestions = Math.toIntExact(snapshot.requiredQuizCount());
		// 사용자의 오답 수
		int userWrongAnswers = Math.toIntExact(snapshot.solvedQuizCount() - snapshot.correctQuizCount());
		return missionFailurePolicy.isMissionFailed(totalQuestions, userWrongAnswers);
	}
}
