package com.oneco.backend.global.config;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@OpenAPIDefinition(
	info = @Info(
		title = "ONECO API 명세서",
		version = "v0",
		description = "ONECO Service Public API Documentation",
		license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0")
	),
	servers = {
		@Server(url = "https://www.oneco.site", description = "Production"),
		@Server(url = "http://localhost:8080", description = "Local")
	},
	// ✅ 전역 기본 보안(원하면 아래 줄 삭제)
	security = @SecurityRequirement(name = "AccessToken")
)
@SecurityScheme(
	name = "AccessToken",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT",
	description = "일반 API 접근용 Access JWT"
)
@SecurityScheme(
	name = "RefreshToken",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT",
	description = "토큰 재발급 전용 Refresh JWT"
)
@SecurityScheme(
	name = "OnboardingToken",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT",
	description = "신규 회원 온보딩 완료 전용 JWT"
)
@Profile({"default", "dev", "staging", "swagger"})
@Configuration
public class SwaggerConfig {

	// === 단일 그룹으로 모든 API 노출 ===
	@Bean
	public GroupedOpenApi onecoApi() {
		return GroupedOpenApi.builder()
			.group("ONECO")
			.displayName("ONECO API")
			.packagesToScan("com.oneco.backend")
			.pathsToMatch("/api/**")
			.build();
	}

	// === 공통 에러 응답 자동 추가 ===
	@Bean
	public OperationCustomizer addGlobalResponses() {
		return (operation, handlerMethod) -> {
			ApiResponse error = new ApiResponse()
				.description("공통 에러 응답")
				.content(new Content().addMediaType(
					"application/json",
					new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
				));
			operation.getResponses().putIfAbsent("400", error);
			operation.getResponses().putIfAbsent("500", error);
			return operation;
		};
	}

	// === 전역 스키마 등록 (ErrorResponse/DataResponse) ===
	@Bean
	public OpenApiCustomizer globalOpenApiCustomizer() {
		return openApi -> {
			Schema<?> errorSchema = new ObjectSchema()
				.addProperty("status", new StringSchema()
					.description("HTTP 상태 구분 문자열 (예: Bad Request, Not Found)")
					.example("Bad Request"))
				.addProperty("timestamp", new StringSchema()
					.description("응답 발생 시각 (RFC3339 포맷, Asia/Seoul 기준)")
					.example("2025-11-11T12:34:56.789+09:00"))
				.addProperty("message", new StringSchema()
					.description("에러 메시지 (사용자 피드백용)")
					.example("사용자를 찾을 수 없습니다."))
				.addProperty("code", new StringSchema()
					.description("도메인 별 세부 오류 코드")
					.example("USER_ERROR_404_NOT_FOUND"))
				.addProperty("reasons", new ObjectSchema()
					.description("필드 검증 실패 등의 상세 사유 (Key-Value 구조)")
					.example(Map.of(
						"email", "올바른 이메일 형식이 아닙니다.",
						"age", "최소 1 이상의 값이어야 합니다."
					)));

			Schema<?> dataSchema = new ObjectSchema()
				.addProperty("status", new StringSchema()
					.description("HTTP 상태 구분 문자열 (예: OK, Created)")
					.example("OK"))
				.addProperty("timestamp", new StringSchema()
					.description("응답 발생 시각 (RFC3339 포맷, Asia/Seoul 기준)")
					.example("2025-11-11T14:25:01.123+09:00"))
				.addProperty("data", new ObjectSchema()
					.description("응답 데이터 (도메인별 DTO 구조)"));

			if (openApi.getComponents() == null) {
				openApi.setComponents(new io.swagger.v3.oas.models.Components()); // Components 객체가 null인 경우 초기화
			}
			openApi.getComponents()
				.addSchemas("ErrorResponse", errorSchema)
				.addSchemas("DataResponse", dataSchema);
		};
	}
}
