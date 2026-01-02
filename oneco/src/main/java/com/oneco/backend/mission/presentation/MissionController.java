package com.oneco.backend.mission.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.global.response.CursorResponse;
import com.oneco.backend.global.response.DataResponse;
import com.oneco.backend.global.security.jwt.JwtPrincipal;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.mission.application.dto.ApproveMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;
import com.oneco.backend.mission.application.port.in.ApproveMissionUseCase;
import com.oneco.backend.mission.application.port.in.CreateMissionUseCase;
import com.oneco.backend.mission.application.service.MissionReadService;
import com.oneco.backend.mission.presentation.request.ApproveMissionRequest;
import com.oneco.backend.mission.presentation.request.CreateMissionRequest;
import com.oneco.backend.mission.presentation.request.MissionCursorRequest;
import com.oneco.backend.mission.presentation.response.MissionResponse;

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
	private final MissionReadService missionReadService;

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

	// ==============================
	// 미션 조회 API(커서 기반 페이징)
	// ==============================

	// 진행중인 미션 조회 API(커서 기반 페이징)
	@GetMapping("/in-progress")
	@Operation(
		summary = "진행중인 미션 조회",
		description = "사용자가 진행중인 미션 목록을 조회한다. 커서(lastId) 쿼리 파라미터로 이어서 조회할 수 있다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "진행중인 미션 조회 성공")
	})
	public ResponseEntity<DataResponse<CursorResponse<MissionResponse>>> getInProgressMissions(
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
		@Parameter(description = "커서 기반 페이징 요청 파라미터") @Valid @ModelAttribute MissionCursorRequest request
	) {
		MemberId memberId = MemberId.of(principal.memberId());
		CursorResponse<MissionResponse> response = missionReadService.getInProgressMissions(memberId, request.lastId(),
			request.size());
		return ResponseEntity.ok(DataResponse.from(response));
	}

	@GetMapping("/finished")
	@Operation(
		summary = "종료된 미션 조회",
		description = "사용자가 종료한 미션 목록을 조회한다. 커서(lastId) 쿼리 파라미터로 이어서 조회할 수 있다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "종료된 미션 조회 성공")
	})
	public ResponseEntity<DataResponse<CursorResponse<MissionResponse>>> getCompletedMissions(
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal,
		@Parameter(description = "커서 기반 페이징 요청 파라미터") @Valid @ModelAttribute MissionCursorRequest request
	) {
		MemberId memberId = MemberId.of(principal.memberId());
		CursorResponse<MissionResponse> response = missionReadService.getFinishedMissions(memberId, request.lastId(),
			request.size());
		return ResponseEntity.ok(DataResponse.from(response));
	}

	// 미션 생성 OK
	// 미션 승인/ 거절 OK
	// 미션 진행중 -> 완료 전환은 API 없음, StudyRecord 도메인에서 (MissionStatusChange을 주입) 에서 처리한다.
	// 미션 진행중 -> 실패 전환(조기 실패)은 API 없음, StudyRecord 도메인에서 (MissionStatusChange을 주입) 에서 처리한다.
	// 미션 진행중 -> 실패 전환(기간 만료)는 API 없음, Mission 도메인에서 MissionBatchService(스케줄러)로 처리한다.
	// 미션 삭제 API는 당장은 없음(미션 기록 보존을 위해 삭제 기능은 추후에 별도로 논의)
}
