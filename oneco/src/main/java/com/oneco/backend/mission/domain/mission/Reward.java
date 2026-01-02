package com.oneco.backend.mission.domain.mission;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Reward {

	private String title;
	private String message;

	protected Reward() {
	} // JPA 기본 생성자

	private Reward(String title, String message) {
		this.title = title;
		this.message = message;
	}

	public static Reward of(String title, String message) {
		return new Reward(title, message);
	}
}
