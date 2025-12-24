package com.oneco.backend.mission.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.global.response.DataResponse;
import com.oneco.backend.global.security.jwt.JwtPrincipal;
import com.oneco.backend.mission.application.dto.ApproveMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;
import com.oneco.backend.mission.application.port.in.ApproveMissionUseCase;
import com.oneco.backend.mission.application.port.in.CreateMissionUseCase;
import com.oneco.backend.mission.presentation.request.ApproveMissionRequest;
import com.oneco.backend.mission.presentation.request.CreateMissionRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "Mission", description = "미션 생성 및 관리")
public class MissionController {

	private final CreateMissionUseCase createMissionUseCase;
	private final ApproveMissionUseCase approveMissionUseCase;

	@GetMapping("/health")
	public String healthCheck() {
		return "미션 서비스 정상 작동 중";
	}

	@PostMapping
	@Operation(
		summary = "미션 생성",
		description = "가족과 카테고리, 기간, 보상 정보를 입력해 미션을 생성한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "미션 생성 성공")
	})
	public ResponseEntity<DataResponse<MissionResult>> createMission(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,
		@RequestBody @Valid CreateMissionRequest request
	) {
		return ResponseEntity.ok(
			DataResponse.from(createMissionUseCase.request(request.toCommand(principal.memberId())))
		);
	}

	@PostMapping("/{missionId}/approval")
	@Operation(
		summary = "미션 승인/거절",
		description = "수신자(recipient)가 미션 승인 요청을 수락하거나 거절한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "미션 승인/거절 처리 성공")
	})
	public ResponseEntity<DataResponse<MissionResult>> decideMissionApproval(
		@Parameter(description = "미션 ID", required = true)
		@PathVariable Long missionId,
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
		@RequestBody @Valid ApproveMissionRequest request
	) {
		ApproveMissionCommand command = request.toCommand(missionId, principal.memberId());
		return ResponseEntity.ok(DataResponse.from(approveMissionUseCase.decide(command)));
	}
}
