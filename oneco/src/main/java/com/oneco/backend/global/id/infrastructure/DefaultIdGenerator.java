package com.oneco.backend.global.id.infrastructure;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.oneco.backend.global.id.domain.IdGenerator;

@Component
public class DefaultIdGenerator implements IdGenerator {

	@Override
	public String generate() {
		return UUID.randomUUID().toString();
	}
}
