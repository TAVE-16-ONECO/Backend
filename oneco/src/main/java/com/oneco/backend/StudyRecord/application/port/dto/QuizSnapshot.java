package com.oneco.backend.StudyRecord.application.port.dto;

import java.util.List;

public record QuizSnapshot(
	Long quizId,
	String question,
	int questionOrder,
	List<String> options
) {
}
