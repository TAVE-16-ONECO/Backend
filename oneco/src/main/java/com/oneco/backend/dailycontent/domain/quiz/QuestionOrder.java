package com.oneco.backend.dailycontent.domain.quiz;

import com.oneco.backend.dailycontent.domain.common.AbstractSequence;

public class QuestionOrder extends AbstractSequence implements Comparable<QuestionOrder> {

	public QuestionOrder(int value) {
		super(value);
	}

	public QuestionOrder next() {
		return new QuestionOrder(nextValue());
	}

	public int compareTo(QuestionOrder o) {
		return Integer.compare(this.value(), o.value());
	}
}
