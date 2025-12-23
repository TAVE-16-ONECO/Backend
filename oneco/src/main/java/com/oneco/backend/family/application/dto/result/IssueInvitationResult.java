package com.oneco.backend.family.application.dto.result;

public record IssueInvitationResult(
	String code,
	long expiresInSeconds
) {}
