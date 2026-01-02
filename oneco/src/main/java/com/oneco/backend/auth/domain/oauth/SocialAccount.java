package com.oneco.backend.auth.domain.oauth;

import com.oneco.backend.global.entity.BaseTimeEntity;
import com.oneco.backend.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
	name = "social_account",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "unq_provider_social",
			columnNames = {"provider", "social_account_id"}
		),
		@UniqueConstraint(
			name = "unq_member_provider",
			columnNames = {"member_id", "provider"}
		)
	}
)
public class SocialAccount extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * N:1 관계
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
		name = "member_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_social_account_member")
	)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SocialProvider provider;

	@Column(name = "social_account_id", nullable = false, length = 255)
	private String socialAccountId;


	private SocialAccount(Member member, SocialProvider provider, String socialAccountId, String profileImageUrl) {
		this.member = member;
		this.provider = provider;
		this.socialAccountId = socialAccountId;
	}

	public static SocialAccount create(Member member, SocialProvider provider, String socialAccountId) {
		SocialAccount socialAccount = new SocialAccount();
		socialAccount.member = member;
		socialAccount.provider = provider;
		socialAccount.socialAccountId = socialAccountId;
		return socialAccount;
	}
}
