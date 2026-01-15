package com.oneco.backend.family.presentation.response;

import java.util.List;

import com.oneco.backend.family.application.dto.result.FamilyMembersResult;

public record FamilyMembersResponse(
	List<MemberResponse> members
) {

	public static FamilyMembersResponse from(FamilyMembersResult result) {
		return new FamilyMembersResponse(
			result.members().stream()
				.map(MemberResponse::from)
				.toList()
		);
	}

	public record MemberResponse(
		Long memberId,
		String nickname,
		String profileImageUrl
	) {
		public static MemberResponse from(FamilyMembersResult.FamilyMemberResult result) {
			return new MemberResponse(result.memberId(), result.nickname(), result.profileImageUrl());
		}
	}

}
