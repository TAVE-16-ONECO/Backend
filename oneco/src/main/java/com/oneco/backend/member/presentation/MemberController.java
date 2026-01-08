package com.oneco.backend.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.global.response.DataResponse;
import com.oneco.backend.global.security.jwt.JwtPrincipal;
import com.oneco.backend.member.application.port.in.MemberUseCase;
import com.oneco.backend.member.presentation.response.MemberInfoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberUseCase memberUseCase;

	@GetMapping("/health")
	public String healthCheck() {
		return "멤버 서비스 정상 작동 중";
	}

	// 회원 정보를 조회하는 API
	@GetMapping("/info")
	@Operation(
		summary = "회원 정보 조회",
		description = """
			현재 로그인한 회원의 정보를 조회한다.\s
			- memberId: 회원 고유 ID\s
			- familyRole: 가족 내 역할\s
			- name: 회원 이름\s
			- nickname: 회원 닉네임(null)\s
			- email: 회원 이메일(null)\s
			- profileImageUrl: 프로필 이미지 URL"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "회원 정보 조회 성공")
	})
	public ResponseEntity<DataResponse<MemberInfoResponse>> getMemberInfo(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		// 현재 로그인한 회원의 ID를 사용하여 회원 정보를 조회
		MemberInfoResponse memberInfo = MemberInfoResponse.from(
			memberUseCase.getMemberInfo(principal.memberId())
		);
		return ResponseEntity.ok(DataResponse.from(memberInfo));

	}

}
