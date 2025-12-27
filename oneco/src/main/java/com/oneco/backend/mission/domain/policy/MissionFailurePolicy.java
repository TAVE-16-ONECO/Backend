package com.oneco.backend.mission.domain.policy;

import static com.oneco.backend.mission.domain.policy.MissionSuccessPolicy.*;

import org.springframework.stereotype.Component;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;
import com.oneco.backend.mission.domain.judge.dto.MissionFailureJudgement;

@Component
public class MissionFailurePolicy {
	// 미션 실패는 다음 조건을 모두 만족해야한다.
	// 성공 가능성이 없을 때 바로 실패로 간주한다.
	// 예를들어 퀴즈가 30개일 때, 성공조건이 정답 개수가 24개 이상이어야한다.
	// 즉, 사용자가 이미 7문제 이상 틀렸을 때, 미션은 실패로 간주된다.
	// 실패 조기 판정 조건: 오답수 > 허용오답수 (허용 오답수 = 총 문항수 - 필요 정답수)

	// 미션 실패 여부 판단 메서드 (필요 정답 수를 직접 전달)
	public MissionFailureJudgement isMissionFailed(int totalQuestions, int userWrongAnswers) {
		validateTotalQuestions(totalQuestions);
		validateUserAnswers(userWrongAnswers);

		// 필요 정답 수 계산
		int requiredCorrectAnswers = calculateRequiredCorrectAnswers(totalQuestions);

		int allowedWrongAnswers = totalQuestions - requiredCorrectAnswers;
		int remainingWrongAnswers = allowedWrongAnswers - userWrongAnswers;
		if (userWrongAnswers > allowedWrongAnswers) {
			return MissionFailureJudgement.fail("허용 오답 수를 초과했습니다.", allowedWrongAnswers, remainingWrongAnswers);
		}

		return MissionFailureJudgement.pass(allowedWrongAnswers, remainingWrongAnswers);
	}

	// 필요 정답 수 계산하는 메서드
	public int calculateRequiredCorrectAnswers(int totalQuestions) {
		validateTotalQuestions(totalQuestions);
		// 예: 30(총 문항수) * 0.8(성공률) = 24(필요 정답수)
		return (int)Math.ceil(totalQuestions * MIN_SUCCESS_RATE); // 올림 처리
	}

	private void validateTotalQuestions(int totalQuestions) {
		if (totalQuestions <= 0) {
			throw BaseException.from(
				MissionErrorCode.INVALID_MISSION_JUDGE,
				"총 문항 수는 1보다 커야합니다. totalQuestions: " + totalQuestions
			);
		}
	}

	private void validateUserAnswers(int userWrongAnswers) {
		if (userWrongAnswers < 0) {
			throw BaseException.from(
				MissionErrorCode.INVALID_MISSION_JUDGE,
				"오답 수는 0 이상이어야 합니다."
			);
		}

	}
}
