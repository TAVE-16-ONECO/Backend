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
		title = "ONECO API DOCS",
		version = "v1",
		description = "ONECO Service Public API Documentation",
		license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0")
	),
	servers = {
		@Server(url = "http://localhost:8080", description = "Local"), // 로컬 환경 URL
		// @Server(url = "https://api.oneco.com", description = "Production") // 운영 환경 URL로 변경 후 주석 해제
	},
	security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT"
)
@Profile({"default", "dev", "staging", "swagger"}) // 운영/로컬/스테이징 + swagger 전용 기동에서 활성화
@Configuration
public class SwaggerConfig {

	// === 모듈/도메인별 Grouping ===
	@Bean
	public GroupedOpenApi authApi() {
		return GroupedOpenApi.builder()
			.group("Auth")
			.displayName("Auth (인증/인가)")
			// 패키지 기준으로 좁히면 스캔 비용↓, 경로 기준 혼용 가능
			.packagesToScan("com.oneco.backend.auth.presentation")
			.pathsToMatch("/api/auth/**")
			.build();
	}

	// 추가 도메인별 GroupedOpenApi 빈 정의 가능

	@Bean
	public OperationCustomizer addGlobalResponses() {
		return (operation, handlerMethod) -> {
			io.swagger.v3.oas.models.responses.ApiResponse error = new ApiResponse()
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

	// === 전역 응답/스키마/정렬 등 커스터마이징 ===
	@Bean
	public OpenApiCustomizer globalOpenApiCustomizer() {
		return openApi -> {
			// ===  ErrorResponse 스키마 ===
			Schema<?> errorSchema = new ObjectSchema()
				// === BaseResponse 필드 ===
				.addProperty("status", new StringSchema()
					.description("HTTP 상태 구분 문자열 (예: Bad Request, Not Found)")
					.example("Bad Request"))
				.addProperty("timestamp", new StringSchema()
					.description("응답 발생 시각 (RFC3339 포맷, Asia/Seoul 기준)")
					.example("2025-11-11T12:34:56.789+09:00"))
				// === ErrorResponse 필드 ===
				.addProperty("message", new StringSchema() // message 필드
					.description("에러 메시지 (사용자 피드백용)")
					.example("사용자를 찾을 수 없습니다."))
				.addProperty("code", new StringSchema() // code 필드
					.description("도메인 별 세부 오류 코드")
					.example("USER_ERROR_404_NOT_FOUND"))
				.addProperty("reasons", new ObjectSchema() // reasons 필드
					.description("필드 검증 실패 등의 상세 사유 (Key-Value 구조)")
					.example(Map.of(
						"email", "올바른 이메일 형식이 아닙니다.",
						"age", "최소 1 이상의 값이어야 합니다."
					)));

			// ===  DataResponse<T> 스키마 ===
			Schema<?> dataSchema = new ObjectSchema()
				.addProperty("status", new StringSchema()
					.description("HTTP 상태 구분 문자열 (예: OK, Created)")
					.example("OK"))
				.addProperty("timestamp", new StringSchema()
					.description("응답 발생 시각 (RFC3339 포맷, Asia/Seoul 기준)")
					.example("2025-11-11T14:25:01.123+09:00"))
				.addProperty("data", new ObjectSchema()
					.description("응답 데이터 (도메인별 DTO 구조)")
					.example(Map.of(
						"userId", 1,
						"email", "user@example.com"
					)));

			openApi.getComponents()
				.addSchemas("ErrorResponse", errorSchema)
				.addSchemas("DataResponse", dataSchema);
		};
	}

}
