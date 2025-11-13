package com.oneco.backend.global.id.infrastructure;

import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;
import com.oneco.backend.global.id.domain.IdGenerator;

public class UuidV7Generator implements IdGenerator {
	@Override
	public String generate() {
		UUID uuid = UuidCreator.getTimeOrderedEpoch(); // UUIDv7 생성
		return uuid.toString();
	}
}
