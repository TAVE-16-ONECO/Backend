package com.oneco.backend.mission.application.port.out;

public record MemberInfo (
	Long id,
	String accountType, // ex) PARENT, CHILD
	String systemRole // USER, ADMIN
) {}
