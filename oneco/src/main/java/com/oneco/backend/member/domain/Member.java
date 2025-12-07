package com.oneco.backend.member.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.oneco.backend.auth.domain.oauth.SocialAccount;
import com.oneco.backend.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member")
public class Member extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name= "family_role", nullable = true, length = 50)
	private FamilyRole familyRole;

	@Enumerated(EnumType.STRING)
	@Column(name = "system_role", nullable = false, length = 50)
	private SystemRole systemRole;

	@Column(name = "name", nullable = true, length = 50)
	private String name;

	@Column(name="nickname", nullable = true, length = 50, unique = true)
	private String nickname;

	@Column(name="email", nullable = true, unique = true, length = 255)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name="status",nullable = false)
	private MemberStatus status;

	@Column(name = "profile_image_url", length = 255)
	private String profileImageUrl;

	// 추후 DDD 스타일로 리팩토링 예정
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<SocialAccount> socialAccounts = new ArrayList<>();


	public static Member createForOnboarding(String profileImageUrl, String nickname,SystemRole systemRole){
		Member member = new Member();
		member.nickname = nickname;
		member.profileImageUrl =profileImageUrl;
		member.status = MemberStatus.ONBOARDING;
		member.systemRole = systemRole;
		return member;
	}

	public void completeOnboarding(FamilyRole familyRole){
		this.familyRole = familyRole;
		this.status = MemberStatus.ACTIVE;
	}
	public void changeStatus(MemberStatus status){
		this.status = status;
	}
}
