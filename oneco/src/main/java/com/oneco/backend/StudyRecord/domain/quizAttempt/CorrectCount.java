package com.oneco.backend.StudyRecord.domain.quizAttempt;

import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 맞춘 개수(0~3)
 * - 제출 전에는 "없을 수" 있으니, 엔티티에서 null 허용으로 두는 걸 추천
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorrectCount {

	private Integer value;
	private static final int MAX_COUNT = 3;
	private static final int MIN_COUNT = 0;

	private CorrectCount(int value) {
		validate(value);
		this.value = value;
	}

	public static CorrectCount of(int value) {
		return new CorrectCount(value);
	}

	private void validate(int value) {
		if (value < MIN_COUNT || value > MAX_COUNT) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION,
				"MIN_COUNT:" + MIN_COUNT + " MAX_COUNT:" + MAX_COUNT + " CURRENT_COUNT:" + value);
		}
	}

	public boolean isPerfect() {
		return value != null && value == 3; // “3문제 다 맞아야 PASS” 정책
	}
}