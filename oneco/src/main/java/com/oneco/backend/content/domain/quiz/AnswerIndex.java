package com.oneco.backend.content.domain.quiz;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerIndex {

	@Column(name = "answer_index", nullable = false)
	private int value;

	public AnswerIndex(int value) {
		if (value < 1) {
			throw new IllegalArgumentException("정답 인덱스는 1 이상이어야 합니다.");
		}
		this.value = value;
	}
}