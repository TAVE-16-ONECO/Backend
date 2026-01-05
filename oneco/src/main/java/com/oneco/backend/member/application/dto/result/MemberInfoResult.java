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
}
