package com.oneco.backend.family.application.port.in;

import com.oneco.backend.family.application.dto.result.FamilyMembersResult;
import com.oneco.backend.member.domain.MemberId;

public interface GetFamilyMembersUseCase {
	FamilyMembersResult getFamilyMembers(MemberId memberId);
}

