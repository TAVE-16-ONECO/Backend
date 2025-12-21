package com.oneco.backend.family.application.dto.command;

import jakarta.validation.constraints.NotNull;

public record DisconnectFamilyRelationCommand(
	@NotNull Long relationId,
	@NotNull Long actorId
) {}
