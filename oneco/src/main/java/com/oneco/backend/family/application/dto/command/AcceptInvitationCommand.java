package com.oneco.backend.family.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcceptInvitationCommand(
	@NotBlank String code,
	@NotNull Long inviteeId
) {}
