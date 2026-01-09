package com.oneco.backend.StudyRecord.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.StudyRecord.application.port.in.GetHomeDashboardUseCase;
import com.oneco.backend.StudyRecord.application.port.in.HomeActiveMissionsUseCase;
import com.oneco.backend.StudyRecord.presentation.response.HomeActiveMissionsResponse;
import com.oneco.backend.StudyRecord.presentation.response.HomeDashboardResponse;
import com.oneco.backend.global.response.DataResponse;
import com.oneco.backend.global.security.jwt.JwtPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "Home", description = "메인 페이지 관련 API")
public class HomeController {

	private final GetHomeDashboardUseCase getHomeDashboardUseCase;
	private final HomeActiveMissionsUseCase homeActiveMissionsUseCase;

	@GetMapping("/dashboard")
	@Operation(
		summary = "홈 대시보드 조회",
		description = "missionId가 없으면 최신 진행중 미션 기준, 있으면 해당 미션 기준으로 홈 대시보드를 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "홈 대시보드 조회 성공")
	})
	public ResponseEntity<DataResponse<HomeDashboardResponse>> getHomeDashboard(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,
		@RequestParam(required = false) Long missionId
	) {
		HomeDashboardResponse response = HomeDashboardResponse.from(
			getHomeDashboardUseCase.getHomeDashboard(principal.memberId(), missionId)
		);
		return ResponseEntity.ok(DataResponse.from(response));
	}

	@GetMapping("/missions/active")
	@Operation(
		summary = "홈 대시보드 API 조회 전 진행중인 미션 조회",
		description = "회원의 진행중인 미션 수와 진행중인 미션 ID 리스트를 조회합니다.\n" +
		"진행중인 미션이 없을 경우, 미션 수는 0, 미션 ID 리스트는 빈 리스트로 반환됩니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "진행중인 미션 조회 성공")
	})
	public ResponseEntity<DataResponse<HomeActiveMissionsResponse>> getActiveMissions(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		HomeActiveMissionsResponse response = HomeActiveMissionsResponse.from(
			homeActiveMissionsUseCase.getActiveMissions(principal.memberId())
		);
		return ResponseEntity.ok(DataResponse.from(response));
	}
}
