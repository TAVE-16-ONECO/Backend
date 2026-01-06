package com.oneco.backend.member.application.port.in;

import com.oneco.backend.member.application.dto.result.MemberInfoResult;

public interface MemberUseCase {

	MemberInfoResult getMemberInfo(Long memberId);
}
