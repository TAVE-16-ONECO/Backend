package com.oneco.backend.StudyRecord.domain.quizAttempt;

import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 몇 번째 시도인지(1부터 시작)
 * - 정책상 최대 2회면 1~2로 검증
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class AttemptNo {

	private int value;

	private static final int MIN_VALUE = 1;
	private static final int MAX_VALUE = 2;

	private AttemptNo(int value) {
		validate(value);
		this.value = value;
	}

	public static AttemptNo of(int value) {
		return new AttemptNo(value);
	}

	private void validate(int value) {
		// 정책이 2회 고정이므로 1~2로 제한
		if (value < MIN_VALUE || value > MAX_VALUE) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION,
				"MIN_VALUE:" + MIN_VALUE + " MAX_VALUE:" + MAX_VALUE + " CURRENT_VALUE:" + value);
		}
	}

	public boolean isFirst() {
		return value == 1;
	}

	public boolean isSecond() {
		return value == 2;
	}
}