package com.oneco.backend.member.application.port.out;

import java.util.Optional;

import com.oneco.backend.member.domain.Member;

public interface MemberPersistencePort {

	Optional<Member> findById(Long memberId);

}
