package com.oneco.backend.member.presentation.response;

public record MemberInfoResponse(
	Long memberId,
	String familyRole,
	String name,
	String nickname,
	String email,
	String profileImageUrl
) {
	public static MemberInfoResponse of(
		Long memberId,
		String familyRole,
		String name,
		String nickname,
		String email,
		String profileImageUrl
	) {
		return new MemberInfoResponse(
			memberId,
			familyRole,
			name,
			nickname,
			email,
			profileImageUrl
		);
	}
}
