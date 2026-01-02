package com.oneco.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@Configuration
public class JpaConfig {
	// BaseTimeEntity 클래스의 @CreatedDate 및 @LastModifiedDate 어노테이션이
	// 자동으로 작동하도록 JPA 감사 기능을 활성화하는 설정 클래스입니다.


}
