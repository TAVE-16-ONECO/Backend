package com.oneco.backend.mission.application.port.out;

import com.oneco.backend.family.domain.relation.FamilyRelationId;
import com.oneco.backend.member.domain.MemberId;

public interface FamilyRelationLookupPort {

	boolean isMembersOfRelation(FamilyRelationId relationId, MemberId requesterId, MemberId recipientId);
}
