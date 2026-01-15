package com.oneco.backend.family.application.port.in;

import java.util.Optional;

import com.oneco.backend.family.application.dto.result.FamilyMembersResult;
import com.oneco.backend.member.domain.MemberId;

public interface GetFamilyMembersUseCase {
	Optional<FamilyMembersResult> getFamilyMembers(MemberId memberId);
}
