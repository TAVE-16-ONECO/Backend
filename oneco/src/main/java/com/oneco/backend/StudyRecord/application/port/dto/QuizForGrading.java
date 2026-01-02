package com.oneco.backend.StudyRecord.application.port.dto;

import java.util.List;

public record QuizForGrading(Long quizId, int correctIndex, List<String> options) {
}
