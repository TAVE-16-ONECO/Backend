package com.oneco.backend.family.presentation.response;

import com.oneco.backend.family.application.dto.result.IssueInvitationResult;

public record InvitationCodeResponse(
	String code,
	long expiresInSeconds
) {
	// 팩토리 메서드 - IssueInvitationResult(Application Layer) -> InvitationCodeResponse(Presentation Layer)
	public static InvitationCodeResponse from(IssueInvitationResult result) {
		return new InvitationCodeResponse(
			result.code(),
			result.expiresInSeconds()
		);
	}
}
