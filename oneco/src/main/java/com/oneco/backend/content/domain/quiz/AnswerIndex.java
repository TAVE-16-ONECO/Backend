package com.oneco.backend.content.domain.quiz;

import com.oneco.backend.content.domain.exception.constant.ContentErrorCode;
import com.oneco.backend.global.exception.BaseException;

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
			throw BaseException.from(ContentErrorCode.ANSWER_INDEX_INVALID);
		}
		this.value = value;
	}
}