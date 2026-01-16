package com.oneco.backend.family.application.port.out;

import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.member.domain.Member;

import java.util.Optional;

// Member 도메인 조회 포트
public interface MemberLookupPort {
	boolean exists(MemberId memberId);

	// 가족 관계 확인
	boolean isParent(MemberId memberId);
	boolean isChild(MemberId memberId);

	Optional<Member> findById(MemberId memberId);
}
