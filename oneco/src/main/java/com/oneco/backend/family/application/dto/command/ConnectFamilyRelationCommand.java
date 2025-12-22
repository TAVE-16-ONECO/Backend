package com.oneco.backend.family.application.dto.command;

import jakarta.validation.constraints.NotNull;

public record ConnectFamilyRelationCommand(
	@NotNull Long parentId,
	@NotNull Long childId
) {}
