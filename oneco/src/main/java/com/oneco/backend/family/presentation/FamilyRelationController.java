package com.oneco.backend.family.presentation;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.family.application.dto.command.AcceptInvitationCommand;
import com.oneco.backend.family.application.dto.command.DisconnectFamilyRelationCommand;
import com.oneco.backend.family.application.dto.command.IssueInvitationCommand;
import com.oneco.backend.family.application.dto.result.FamilyRelationResult;
import com.oneco.backend.family.application.dto.result.IssueInvitationResult;
import com.oneco.backend.family.application.port.in.AcceptInvitationUseCase;
import com.oneco.backend.family.application.port.in.DisconnectFamilyRelationUseCase;
import com.oneco.backend.family.application.port.in.ExistsFamilyRelationUseCase;
import com.oneco.backend.family.application.port.in.IssueInvitationUseCase;
import com.oneco.backend.family.presentation.request.AcceptInvitationRequest;
import com.oneco.backend.family.presentation.response.FamilyRelationExists;
import com.oneco.backend.global.response.DataResponse;
import com.oneco.backend.global.security.jwt.JwtPrincipal;
import com.oneco.backend.member.domain.MemberId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family")
@Tag(name = "Family", description = "가족 초대 및 관계 관리")
public class FamilyRelationController {

	private final DisconnectFamilyRelationUseCase disconnectUseCase;
	private final IssueInvitationUseCase issueInvitationUseCase;
	private final AcceptInvitationUseCase acceptInvitationUseCase;
	private final ExistsFamilyRelationUseCase existsFamilyRelationUseCase;

	@GetMapping("/invitations/code")
	@Operation(
		summary = "내 초대 코드 조회",
		description = "현재 로그인한 사용자에게 유효한 초대 코드를 반환한다." +
			"이미 존재하면 기존 코드를 반환하고, 없으면 생성 후 반환한다(멱등)."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "초대 코드 조회 성공")
	})
	public ResponseEntity<DataResponse<IssueInvitationResult>> getMyInvitationCode(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		IssueInvitationResult result = issueInvitationUseCase.issue(
			new IssueInvitationCommand(principal.memberId())
		);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue());
		headers.add(HttpHeaders.PRAGMA, "no-cache");
		headers.add(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION); // 사용자별 응답 분리

		return ResponseEntity.ok().
			headers(headers).
			body(DataResponse.from(result));
	}

	@PostMapping("/invitations/accept")
	@Operation(
		summary = "가족 초대 수락",
		description = "초대 코드와 로그인한 사용자를 연결해 가족 관계를 생성한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "가족 관계 생성 및 상태 반환")
	})
	public ResponseEntity<DataResponse<FamilyRelationResult>> accept(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,
		@RequestBody @Valid AcceptInvitationRequest request
	) {
		AcceptInvitationCommand command = new AcceptInvitationCommand(
			request.code(),
			principal.memberId()
		);

		return ResponseEntity.ok(DataResponse.from(acceptInvitationUseCase.accept(command)));
	}

	@PatchMapping("/relations/{relationId}/disconnect") // soft delete(상태전이)이므로 PATCH
	@Operation(
		summary = "가족 관계 해제",
		description = "가족 관계를 소프트 삭제(상태 전이)하여 더 이상 연결되지 않도록 처리한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "관계 해제 성공")
	})
	public ResponseEntity<DataResponse<FamilyRelationResult>> disconnect(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,
		@Parameter(description = "가족 관계 식별자", required = true)
		@PathVariable Long relationId
	) {
		DisconnectFamilyRelationCommand command = new DisconnectFamilyRelationCommand(
			relationId,
			principal.memberId()
		);

		return ResponseEntity.ok(DataResponse.from(disconnectUseCase.disconnect(command)));
	}

	@GetMapping("/exists")
	@Operation(
		summary = "가족 관계 존재 여부 확인",
		description = "현재 로그인한 사용자가 가족 관계를 맺고 있는지 여부를 반환한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "가족 관계 존재 여부 반환")
	})
	public ResponseEntity<DataResponse<FamilyRelationExists>> existsFamilyRelation(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		// 현재 로그인한 사용자의 가족 관계 존재 여부 확인
		FamilyRelationExists exists = existsFamilyRelationUseCase.existsFamilyRelation(
			MemberId.of(principal.memberId())
		);
		return ResponseEntity.ok(DataResponse.from(exists));
	}
}