package com.oneco.backend.member.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.oneco.backend.member.application.port.out.MemberPersistencePort;
import com.oneco.backend.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberPersistencePort {

	private final MemberJpaRepository repository;

	@Override
	public Optional<Member> findById(Long memberId) {
		return repository.findById(memberId);
	}
}

