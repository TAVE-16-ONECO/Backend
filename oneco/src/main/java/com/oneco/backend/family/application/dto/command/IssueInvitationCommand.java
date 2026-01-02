package com.oneco.backend.family.application.dto.command;

import jakarta.validation.constraints.NotNull;

public record IssueInvitationCommand(
	@NotNull Long inviterId
) {}
