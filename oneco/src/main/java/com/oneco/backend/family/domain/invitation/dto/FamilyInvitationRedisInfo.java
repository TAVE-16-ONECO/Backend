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
}
