package com.oneco.backend.auth.application.bridge;

import com.oneco.backend.auth.application.dto.KakaoLoginResponse;

public interface LoginBridgeStore {
	String save(KakaoLoginResponse response);
	KakaoLoginResponse consume(String key); // 1회성
}
