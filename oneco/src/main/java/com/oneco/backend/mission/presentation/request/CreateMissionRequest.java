package com.oneco.backend.mission.presentation.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.oneco.backend.mission.application.dto.CreateMissionCommand;

public record CreateMissionRequest(
	@NotNull @Positive Long recipientId,
	@NotNull @Positive Long categoryId,

	// Mission Period
	@NotNull LocalDate startDate,
	@NotNull LocalDate endDate,

	// Reward
	@NotBlank @Size(max = 100) String title,
	@Size(max = 255) String message // Nullable
	) {

	public CreateMissionCommand toCommand(Long requesterId) {
		return new CreateMissionCommand(
			requesterId,
			recipientId,
			categoryId,
			startDate,
			endDate,
			title,
			message
		);
	}
}
