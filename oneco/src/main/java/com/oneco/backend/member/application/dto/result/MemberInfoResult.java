package com.oneco.backend.member.application.dto.result;

import com.oneco.backend.member.domain.FamilyRole;

public record MemberInfoResult(
	Long memberId,
	FamilyRole familyRole,
	String name,
	String nickname,
	String email,
	String profileImageUrl
) {
	public static MemberInfoResult of(
		Long memberId,
		FamilyRole familyRole,
		String name,
		String nickname,
		String email,
		String profileImageUrl
	) {
		return new MemberInfoResult(
			memberId,
			familyRole,
			name,
			nickname,
			email,
			profileImageUrl
		);
	}
}
