package com.oneco.backend.dailycontent.domain.dailycontent;

import com.oneco.backend.dailycontent.domain.common.AbstractSequence;

/**
 * DaySequence는 1부터 시작하는 일차(순번) 값을 나타내는 값 객체입니다.
 * 이 클래스는 AbstractSequence를 상속하며, 다음 일차 값을 생성하는 메서드를 제공합니다.
 * Comparable 인터페이스를 구현하여 DaySequence 객체 간의 비교가 가능합니다.
 * Comparable이 없으면 기본 정렬이 불가능하여, 정렬이 필요한 컬렉션에서 사용할 수 없습니다.
 */
public class DaySequence extends AbstractSequence implements Comparable<DaySequence> {

	public DaySequence(int value) {
		super(value);
	}

	public DaySequence next() {
		return new DaySequence(nextValue());
	}

	@Override
	public int compareTo(DaySequence o) {
		return Integer.compare(this.value(), o.value());
	}
}
