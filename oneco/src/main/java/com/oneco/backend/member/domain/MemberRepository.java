package com.oneco.backend.member.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneco.backend.auth.domain.oauth.SocialProvider;

public interface MemberRepository extends JpaRepository<Member,Long> {

}

