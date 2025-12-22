package com.oneco.backend.family.presentation.request;

import jakarta.validation.constraints.NotNull;

public record DisconnectFamilyRelationRequest(
	@NotNull Long relationId
) {
}
