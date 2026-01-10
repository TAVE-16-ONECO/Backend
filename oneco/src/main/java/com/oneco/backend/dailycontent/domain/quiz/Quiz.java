package com.oneco.backend.dailycontent.domain.quiz;

import com.oneco.backend.dailycontent.domain.exception.constant.ContentErrorCode;
import com.oneco.backend.dailycontent.infrastructure.converter.QuestionOrderConverter;
import com.oneco.backend.dailycontent.infrastructure.converter.QuizOptionsConverter;
import com.oneco.backend.global.exception.BaseException;

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
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING);
		}

		if (options == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING);
		}

		if (answerIndex == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING);
		}

		if (question == null || question.isBlank()) {
			throw BaseException.from(ContentErrorCode.QUIZ_QUESTION_EMPTY);
		}

		int optionCount = options.getOptions().size();
		if (answerIndex.getValue() < 1 || answerIndex.getValue() > optionCount) {
			throw BaseException.from(ContentErrorCode.ANSWER_INDEX_OUT_OF_RANGE);
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
			throw BaseException.from(ContentErrorCode.QUIZ_QUESTION_EMPTY);
		}
		this.question = newQuestion.trim();
	}

}
