package com.oneco.backend.content.domain.quiz;

import com.oneco.backend.content.domain.common.AbstractSequence;

public class QuestionOrder extends AbstractSequence implements Comparable<QuestionOrder>{

	public QuestionOrder(int value) {
		super(value);
	}

	public QuestionOrder next(){
		return new QuestionOrder(nextValue());
	}

	public int compareTo(QuestionOrder o){
		return Integer.compare(this.value(), o.value());
	}
}
