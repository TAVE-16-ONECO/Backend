package com.oneco.backend.member.presentation.response;

import com.oneco.backend.member.application.dto.result.MemberInfoResult;

public record MemberInfoResponse(
	Long memberId,
	String familyRole,
	String name,
	String nickname,
	String email,
	String profileImageUrl
) {
	public static MemberInfoResponse from(MemberInfoResult memberInfoResult) {
		return new MemberInfoResponse(
			memberInfoResult.memberId(),
			memberInfoResult.familyRole().name(),
			memberInfoResult.name(),
			memberInfoResult.nickname(),
			memberInfoResult.email(),
			memberInfoResult.profileImageUrl()
		);
	}
}
