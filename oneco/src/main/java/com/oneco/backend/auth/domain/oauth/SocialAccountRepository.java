package com.oneco.backend.auth.domain.oauth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long>{
	Optional<SocialAccount> findByProviderAndSocialAccountId(SocialProvider provider, String socialAccountId);

}
