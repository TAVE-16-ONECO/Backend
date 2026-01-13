package com.oneco.backend.member.domain;

public enum FamilyRole {
	PARENT, CHILD;

	public static FamilyRole parseRole(String raw) {
		String role = raw.startsWith("ROLE_") ? raw.substring(5) : raw;
		return FamilyRole.valueOf(role);
	}
}
