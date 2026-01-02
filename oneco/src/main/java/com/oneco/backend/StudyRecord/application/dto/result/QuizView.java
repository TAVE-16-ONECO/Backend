package com.oneco.backend.StudyRecord.application.dto.result;

import java.util.List;

/**
 * 예시
 * {
 * "quizId": 1001,
 * "question": "What is the capital of France?",
 * "options": [
 * "A",
 * "B"
 * ]}
 */
public record QuizView(
	Long quizId,
	String question,
	List<String> options
) {
}