package com.oneco.backend.StudyRecord.application.port.dto;

public record ActiveMissionSnapshot(
	Long missionId,
	Long categoryId,
	boolean active,
	int openedDaySequence
) {
}
