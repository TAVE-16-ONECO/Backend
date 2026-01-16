package com.oneco.backend.family.application.dto.result;

import java.util.List;

public record FamilyMembersResult(
	List<FamilyMemberResult> members
) {

	public static FamilyMembersResult of(List<FamilyMemberResult> members) {
		return new FamilyMembersResult(List.copyOf(members));
	}

	public record FamilyMemberResult(
		Long memberId,
		String nickname,
		String profileImageUrl
	) {
		public static FamilyMemberResult of(Long memberId, String nickname, String profileImageUrl) {
			return new FamilyMemberResult(memberId, nickname, profileImageUrl);
		}
	}
}
