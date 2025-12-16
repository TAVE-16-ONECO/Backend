package com.oneco.backend.global.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BearerTokenExtractorTest {

	@Test
	void extractOrNull_returnsTokenWhenPrefixPresent() {
		String token = BearerTokenExtractor.extractOrNull("Bearer abc.def.ghi");

		assertEquals("abc.def.ghi", token);
	}

	@Test
	void extractOrNull_returnsNullWhenHeaderInvalid() {
		assertNull(BearerTokenExtractor.extractOrNull(null));
		assertNull(BearerTokenExtractor.extractOrNull(""));
		assertNull(BearerTokenExtractor.extractOrNull("Basic abc"));
		assertNull(BearerTokenExtractor.extractOrNull("Bearer    "));
	}

	@Test
	void extractOrThrow_throwsWhenHeaderMissing() {
		assertThrows(IllegalArgumentException.class, () -> BearerTokenExtractor.extractOrThrow("Basic abc"));
		assertThrows(IllegalArgumentException.class, () -> BearerTokenExtractor.extractOrThrow(null));
	}
}
