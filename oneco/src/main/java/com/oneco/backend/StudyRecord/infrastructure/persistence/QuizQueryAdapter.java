package com.oneco.backend.StudyRecord.infrastructure.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.result.QuizView;
import com.oneco.backend.StudyRecord.application.port.dto.QuizForGrading;
import com.oneco.backend.StudyRecord.application.port.out.QuizQueryPort;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.dailycontent.domain.quiz.Quiz;
import com.oneco.backend.dailycontent.infrastructure.persistence.QuizJpaRepository;
import com.oneco.backend.global.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuizQueryAdapter implements QuizQueryPort {

	private final QuizJpaRepository quizJpaRepository;

	@Override
	@Transactional(readOnly = true)
	public List<QuizForGrading> loadQuizzesForGradingByIds(List<Long> quizIds) {
		List<Quiz> quizzes = quizJpaRepository.findByIdIn(quizIds);

		if (quizzes.size() != quizIds.size()) {
			throw BaseException.from(StudyErrorCode.QUIZ_NOT_FOUND,
				"quizIds size=" + quizIds.size() + ", loaded size=" + quizzes.size());
		}

		// 재정렬: quizIds 순서대로
		Map<Long, Quiz> map = new HashMap<>();
		for (Quiz q : quizzes) {
			Long id = q.getId();

			if (!map.containsKey(id)) {
				map.put(id, q);
			}
		}

		List<QuizForGrading> result = new ArrayList<>();
		for (Long id : quizIds) {
			Quiz q = map.get(id);
			if (q == null) {
				throw BaseException.from(StudyErrorCode.QUIZ_NOT_FOUND);
			}
			QuizForGrading qfg = new QuizForGrading(
				q.getId(),
				q.getAnswerIndex().getValue(),
				q.getOptions().getOptionTexts()
			);
			result.add(qfg);
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<QuizView> loadQuizzesByIds(List<Long> quizIds) {
		List<Quiz> quizzes = quizJpaRepository.findByIdIn(quizIds);

		if (quizzes.size() != quizIds.size()) {
			throw BaseException.from(StudyErrorCode.QUIZ_NOT_FOUND,
				"quizIds size=" + quizIds.size() + ", loaded size=" + quizzes.size());
		}

		// 재정렬: qu
		Map<Long, Quiz> map = new HashMap<>();
		for (Quiz q : quizzes) {
			Long id = q.getId();

			if (!map.containsKey(id)) {
				map.put(id, q);
			}
		}

		List<QuizView> result = new ArrayList<>();
		for (Long id : quizIds) {
			Quiz q = map.get(id);
			if (q == null) {
				throw BaseException.from(StudyErrorCode.QUIZ_NOT_FOUND);
			}
			QuizView qv = new QuizView(
				q.getId(),
				q.getQuestion(),
				q.getOptions().getOptionTexts()
			);
			result.add(qv);
		}
		return result;
	}
}
