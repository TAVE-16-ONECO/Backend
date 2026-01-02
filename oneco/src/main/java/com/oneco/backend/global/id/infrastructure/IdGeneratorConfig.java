package com.oneco.backend.global.id.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oneco.backend.global.id.domain.IdGenerator;

@Configuration
public class IdGeneratorConfig {

	@Bean(name = "uuidV7Generator")
	@ConditionalOnProperty(name = "id.generator.version", havingValue = "v7") // id.generator.version 프로퍼티가 v7일 때만 생성
	public IdGenerator uuidV7IdGenerator() {
		return new UuidV7Generator();
	}

	@Bean
	@ConditionalOnMissingBean(IdGenerator.class) // 다른 IdGenerator 빈이 없을 때만 생성
	public IdGenerator defaultIdGenerator() {
		return new DefaultIdGenerator();
	}
}
