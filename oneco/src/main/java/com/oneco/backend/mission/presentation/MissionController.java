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
import com.oneco.backend.mission.presentation.response.MissionCountResponse;
import com.oneco.backend.mission.presentation.response.MissionDetailResponse;
import com.oneco.backend.mission.presentation.response.MissionExistsResponse;
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

	@PostMapping
	@Operation(
		summary = "회원이 미션을 생성한다.",
		description = """
			- 미션 생성 요청을 처리한다.
			- 미션 생성 시 가족 구성원 중 한명을 수신자로 지정한다.
			- 생성된 미션은 수신자에게 할당된다.
			- 요청자는 미션의 세부 내용을 포함하여 미션을 생성할 수 있다.
			- 생성된 미션의 상태는 '승인요청' 상태로 시작된다.
			"""
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
		summary = "회원이 미션 승인/거절 처리한다.",
		description = """
			- 미션 승인 또는 거절 요청을 처리한다.
			- 미션 수신자는 미션을 승인하거나 거절할 수 있다.
			- 승인 시 미션 상태는 '승인 수락'으로 변경된다.
			- 거절 시 미션 상태는 '거절됨'으로 변경된다.
			- 미션 승인 시점이 미션 시작일이 같을 경우 미션 상태는 즉시 '진행중'으로 변경된다.
			- 미션 승인 시점이 미션 시작일보다 이후 일 경우 미션 상태는 즉시 '진행중'으로 변경된다.
			"""
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

	@GetMapping("/in-progress")
	@Operation(
		summary = "회원의 진행중인 미션을 조회한다.[커서 기반 페이징]",
		description = """
			- 사용자의 진행중인 미션 목록을 조회한다.
			- 진행중인 미션은 승인 대기, 진행중 상태를 포함한다
			- 커서(lastId) 쿼리 파라미터로 이어서 조회할 수 있다.
			- 크기(size) 쿼리 파라미터로 한 번에 조회할 개수를 지정할 수 있다.
			- size의 기본 값은 5이며, size 파라미터를 생략하면 기본값이 적용된다.
			- LastId는 생략할 수 있다. 생략 시 가장 최신의 진행중인 미션부터 조회를 시작한다.(초기 요청)"""
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
		summary = "회원의 종료된 미션을 조회한다.[커서 기반 페이징]",
		description = """
			- 사용자의 종료된 미션 목록을 조회한다.
			- 종료된 미션은 승인 거절, 미션 완료/실패, 보상 요청, 보상 수령 상태를 포함한다.
			- 커서(lastId) 쿼리 파라미터로 이어서 조회할 수 있다.
			- 크기(size) 쿼리 파라미터로 한 번에 조회할 개수를 지정할 수 있다.
			- size의 기본 값은 5이며, size 파라미터를 생략하면 기본값이 적용된다.
			- LastId는 생략할 수 있다. 생략 시 가장 최신의 종료된 미션부터 조회를 시작한다.(초기 요청)"""
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

	@GetMapping("me/count")
	@Operation(
		summary = "회원의 미션 개수를 조회한다.",
		description = """
			- 회원의 미션 개수를 상태별로 조회한다.
			- 진행중인 미션, 종료된 미션, 미션 합계 개수를 반환한다."""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "미션 개수 조회 성공")
	})
	public ResponseEntity<DataResponse<MissionCountResponse>> getMyMissionCount(
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal
	) {
		MemberId memberId = MemberId.of(principal.memberId());
		MissionCountResponse response = missionReadService.countMyMissions(memberId);
		return ResponseEntity.ok(DataResponse.from(response));
	}

	// 회원의 진행중인 미션이 있는지 확인하는 API
	@GetMapping("/exists-in-progress")
	@Operation(
		summary = "진행중인 미션 존재 여부 확인",
		description = "사용자가 진행중인 미션이 있는지 여부를 확인한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "진행중인 미션 존재 여부 확인 성공")
	})
	public ResponseEntity<DataResponse<MissionExistsResponse>> existsInProgressMission(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		MemberId memberId = MemberId.of(principal.memberId());
		MissionExistsResponse response = missionReadService.existsInProgressMission(memberId);
		return ResponseEntity.ok(DataResponse.from(response));
	}

	// missionId로 미션 단건 조회(미션 상세 조회용)
	// 미션의 categoryTitle, rewardTitle, startDate, endDate를 포함한다.
	@GetMapping("/{missionId}")
	@Operation(
		summary = "미션 단건 조회",
		description = "미션 ID로 미션의 세부 정보를 조회한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "미션 단건 조회 성공")
	})
	public ResponseEntity<DataResponse<MissionDetailResponse>> getMissionById(
		@Parameter(description = "미션 ID", required = true)
		@PathVariable Long missionId,
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		MemberId memberId = MemberId.of(principal.memberId());
		MissionDetailResponse response = missionReadService.getMissionDetailById(memberId, missionId);
		return ResponseEntity.ok(DataResponse.from(response));
	}
}
