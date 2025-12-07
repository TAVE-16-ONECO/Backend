package com.oneco.backend.auth.infrastructure.oauth.kakao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix= "kakao.kapi")
public class KakaoKApiProperties {
	private String baseUrl;
}
