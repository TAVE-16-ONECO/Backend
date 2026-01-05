package com.oneco.backend.member.application.port.in;

import com.oneco.backend.member.presentation.response.MemberInfoResponse;

public interface MemberUseCase {

	MemberInfoResponse getMemberInfo(Long memberId);
}
