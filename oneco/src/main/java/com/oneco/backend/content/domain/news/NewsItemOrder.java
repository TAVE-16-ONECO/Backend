package com.oneco.backend.content.domain.news;

import com.oneco.backend.content.domain.common.AbstractSequence;

public class NewsItemOrder extends AbstractSequence implements Comparable<NewsItemOrder> {

	public NewsItemOrder(int value) {
		super(value);
	}

	public NewsItemOrder next(){
		return new NewsItemOrder(nextValue());
	}

	public int compareTo(NewsItemOrder o){
		return Integer.compare(this.value(), o.value());
	}
}

