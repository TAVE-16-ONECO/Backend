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
import com.oneco.backend.family.application.port.in.AcceptInvitationUseCase;
import com.oneco.backend.family.application.port.in.DisconnectFamilyRelationUseCase;
import com.oneco.backend.family.application.port.in.ExistsFamilyRelationUseCase;
import com.oneco.backend.family.application.port.in.GetFamilyMembersUseCase;
import com.oneco.backend.family.application.port.in.IssueInvitationUseCase;
import com.oneco.backend.family.presentation.request.AcceptInvitationRequest;
import com.oneco.backend.family.presentation.response.FamilyMembersResponse;
import com.oneco.backend.family.presentation.response.FamilyRelationResponse;
import com.oneco.backend.family.presentation.response.InvitationCodeResponse;
import com.oneco.backend.family.presentation.response.FamilyRelationExistsResponse;
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
	private final GetFamilyMembersUseCase getFamilyMembersUseCase;

	@GetMapping("/invitations/code")
	@Operation(
		summary = "내 초대 코드 조회",
		description = "현재 로그인한 사용자에게 유효한 초대 코드를 반환한다." +
			"이미 존재하면 기존 코드를 반환하고, 없으면 생성 후 반환한다(멱등)."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "초대 코드 조회 성공")
	})
	public ResponseEntity<DataResponse<InvitationCodeResponse>> getMyInvitationCode(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {

		IssueInvitationCommand command = IssueInvitationCommand.of(principal.memberId());
		InvitationCodeResponse response = InvitationCodeResponse.from(issueInvitationUseCase.issue(command));

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue());
		headers.add(HttpHeaders.PRAGMA, "no-cache");
		headers.add(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION); // 사용자별 응답 분리

		return ResponseEntity.ok().
			headers(headers).
			body(DataResponse.from(response));
	}

	@PostMapping("/invitations/accept")
	@Operation(
		summary = "가족 초대 수락",
		description = "초대 코드와 로그인한 사용자를 연결해 가족 관계를 생성한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "가족 관계 생성 및 상태 반환")
	})
	public ResponseEntity<DataResponse<FamilyRelationResponse>> accept(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,
		@RequestBody @Valid AcceptInvitationRequest request
	) {
		AcceptInvitationCommand command = AcceptInvitationCommand.of(
			request.code(),
			principal.memberId()
		);

		FamilyRelationResponse response = FamilyRelationResponse.from(
			acceptInvitationUseCase.accept(command)
		);

		return ResponseEntity.ok(DataResponse.from(response));
	}

	@PatchMapping("/relations/{relationId}/disconnect") // soft delete(상태전이)이므로 PATCH
	@Operation(
		summary = "가족 관계 해제",
		description = "가족 관계를 소프트 삭제(상태 전이)하여 더 이상 연결되지 않도록 처리한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "관계 해제 성공")
	})
	public ResponseEntity<DataResponse<FamilyRelationResponse>> disconnect(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,
		@Parameter(description = "가족 관계 식별자", required = true)
		@PathVariable Long relationId
	) {
		DisconnectFamilyRelationCommand command = DisconnectFamilyRelationCommand.of(
			relationId,
			principal.memberId()
		);

		FamilyRelationResponse response = FamilyRelationResponse.from(
			disconnectUseCase.disconnect(command)
		);

		return ResponseEntity.ok(DataResponse.from(response));
	}

	@GetMapping("/exists")
	@Operation(
		summary = "가족 관계 존재 여부 확인",
		description = "현재 로그인한 사용자가 가족 관계를 맺고 있는지 여부를 반환한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "가족 관계 존재 여부 반환")
	})
	public ResponseEntity<DataResponse<FamilyRelationExistsResponse>> existsFamilyRelation(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		// 현재 로그인한 사용자의 가족 관계 존재 여부 확인
		FamilyRelationExistsResponse response = FamilyRelationExistsResponse.of(
			existsFamilyRelationUseCase.existsFamilyRelation(MemberId.of(principal.memberId()))
		);

		return ResponseEntity.ok(DataResponse.from(response));
	}

	@GetMapping("/members")
	@Operation(
		summary = "연결된 가족 조회",
		description = "현재 로그인한 사용자가 연결된 가족 정보를 반환한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "연결된 가족 정보 반환"),
		@ApiResponse(responseCode = "404", description = "연결된 가족이 존재하지 않는 경우")
	})
	public ResponseEntity<DataResponse<FamilyMembersResponse>> getConnectedFamilyMembers(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		FamilyMembersResponse response = FamilyMembersResponse.from(
			getFamilyMembersUseCase.getFamilyMembers(MemberId.of(principal.memberId()))
		);

		return ResponseEntity.ok(DataResponse.from(response));
	}
}
