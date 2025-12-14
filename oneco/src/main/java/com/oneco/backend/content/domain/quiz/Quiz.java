package com.oneco.backend.content.domain.quiz;

import com.oneco.backend.content.infrastructure.converter.QuestionOrderConverter;
import com.oneco.backend.content.infrastructure.converter.QuizOptionsConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quizzes",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_daily_question_order",
			columnNames = {"daily_content_id", "question_order"}
		)
	})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 현재 단계에서는 String
	// 추후 Question 같은 별도 값 객체로 분리할 수도 있음
	@Column(name = "question", nullable = false, length = 500)
	private String question;

	@Convert(converter = QuestionOrderConverter.class)
	@Column(name = "question_order", nullable = false)
	private QuestionOrder questionOrder;

	@Embedded
	private AnswerIndex answerIndex;

	@Convert(converter = QuizOptionsConverter.class)
	@Column(name = "options_json", nullable = false, columnDefinition = "TEXT")
	private QuizOptions options;

	private Quiz(String question, QuestionOrder questionOrder, QuizOptions options, AnswerIndex answerIndex) {
		if (questionOrder == null) {
			throw new IllegalArgumentException("questionOrder는 null일 수 없습니다.");
		}
		if (options == null) {
			throw new IllegalArgumentException("options는 null일 수 없습니다.");
		}
		if (answerIndex == null) {
			throw new IllegalArgumentException("answerIndex는 null일 수 없습니다.");
		}
		if (question == null || question.isBlank()) {
			throw new IllegalArgumentException("question은 비어 있을 수 없습니다.");
		}
		int optionCount = options.getOptions().size();
		if (answerIndex.getValue() < 1 || answerIndex.getValue() > optionCount) {
			throw new IllegalArgumentException("answerIndex가 options의 범위를 벗어났습니다.");
		}
		this.question = question.trim();
		this.questionOrder = questionOrder;
		this.options = options;
		this.answerIndex = answerIndex;
	}

	public static Quiz create(String question, QuestionOrder questionOrder, QuizOptions options,
		AnswerIndex answerIndex) {
		return new Quiz(question, questionOrder, options, answerIndex);
	}

	public void changeQuestion(String newQuestion) {
		if (newQuestion == null || newQuestion.isBlank()) {
			throw new IllegalArgumentException("question은 비어 있을 수 없습니다.");
		}
		this.question = newQuestion.trim();
	}

}
