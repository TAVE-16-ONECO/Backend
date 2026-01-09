package com.oneco.backend.dailycontent.domain.news;

import com.oneco.backend.dailycontent.domain.common.AbstractSequence;

public class NewsItemOrder extends AbstractSequence implements Comparable<NewsItemOrder> {

	public NewsItemOrder(int value) {
		super(value);
	}

	public NewsItemOrder next() {
		return new NewsItemOrder(nextValue());
	}

	public int compareTo(NewsItemOrder o) {
		return Integer.compare(this.value(), o.value());
	}
}

