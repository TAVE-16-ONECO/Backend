package com.oneco.backend.family.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(
	@NotBlank String code
) {
}
