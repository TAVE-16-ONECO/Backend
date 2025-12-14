package com.oneco.backend.auth.application.bridge;

public interface OAuthStateStore {
	String getExpectedState(String stateKey);

	void remove(String stateKey);
}


