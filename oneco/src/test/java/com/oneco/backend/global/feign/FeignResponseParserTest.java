package com.oneco.backend.global.feign;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneco.backend.auth.infrastructure.oauth.kakao.dto.KakaoOAuthErrorResponse;

import feign.Request;
import feign.Response;

class FeignResponseParserTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void parseBody_whenBodyPresent_returnsRawAndDto() throws IOException {
		String json = """
			{"error":"invalid_grant","error_description":"authorization code not found"}
			""";
		Response response = Response.builder()
			.status(400)
			.reason("Bad Request")
			.request(Request.create(Request.HttpMethod.GET, "/oauth", Collections.emptyMap(), null,
				StandardCharsets.UTF_8, null))
			.body(json, StandardCharsets.UTF_8)
			.build();

		FeignResponseParser.ParsedBody<KakaoOAuthErrorResponse> parsed =
			FeignResponseParser.parseBody(response, objectMapper, KakaoOAuthErrorResponse.class);

		assertEquals(json.trim(), parsed.rawBody().trim());
		assertEquals("invalid_grant", parsed.body().error());
		assertEquals("authorization code not found", parsed.body().errorDescription());
	}

	@Test
	void parseBody_whenBodyMissing_returnsEmptyRawAndNullDto() throws IOException {
		Response response = Response.builder()
			.status(204)
			.reason("No Content")
			.request(Request.create(Request.HttpMethod.GET, "/oauth", Collections.emptyMap(), null,
				StandardCharsets.UTF_8, null))
			.build();

		FeignResponseParser.ParsedBody<KakaoOAuthErrorResponse> parsed =
			FeignResponseParser.parseBody(response, objectMapper, KakaoOAuthErrorResponse.class);

		assertEquals("", parsed.rawBody());
		assertNull(parsed.body());
	}
}
