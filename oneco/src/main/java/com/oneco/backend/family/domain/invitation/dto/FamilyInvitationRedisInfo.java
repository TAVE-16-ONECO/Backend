package com.oneco.backend.family.domain.invitation.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor // 역직렬화 Redis -> Java
@AllArgsConstructor
public class FamilyInvitationRedisInfo implements Serializable {

	private Long inviterId;
	private Long familyId;

	/* 서비스 계층에서 사용 예시
	 저장할 때 (Service)
	 FamilyInvitationRedisInfo info = FamilyInvitationRedisInfo.builder()
	 	.inviterId(inviterId.getValue()) // MemberId -> Long 추출
	 	.familyId(familyId.getValue())   // FamilyRelationId -> Long 추출
	 	.build();

	 // 꺼낼 때 (Service)
	 FamilyInvitationRedisInfo info = redisService.getInvitation(code);
	 MemberId inviter = MemberId.from(info.getInviterId()); // Long -> MemberId 복원
	 */
}
