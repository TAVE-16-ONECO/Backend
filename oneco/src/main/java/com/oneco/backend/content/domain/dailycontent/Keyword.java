package com.oneco.backend.content.domain.dailycontent;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Keyword {
	public static final int MAX_LENGTH = 100;

	@Column(name = "keyword", nullable = false, length = 100)
	private String value;

	private Keyword(String value){
		if(value == null || value.isBlank()){
			throw new IllegalArgumentException("키워드는 비어있을 수 없습니다.");
		}
		String v= value.trim();
		if(v.length() > MAX_LENGTH){
			throw new IllegalArgumentException("키워드는 최대 " + MAX_LENGTH+"자까지 허용됩니다.");
		}
		this.value = v;
	}

	public static Keyword of(String value){
		return new Keyword(value);
	}
}
