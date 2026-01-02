package com.oneco.backend.StudyRecord.application.port.out;

import java.util.List;

import com.oneco.backend.StudyRecord.application.dto.result.QuizView;
import com.oneco.backend.StudyRecord.application.port.dto.QuizForGrading;

/**
 * quizIds로 Quiz를 조회하는 Port
 * - RETRY_AVAILABLE 시에는 attempt.quizIds로 Quiz를 다시 로드해야 퀴즈 화면을 구성할 수 있다.
 * - 제출 시에는 정답/보기개수 검증을 위해 필요하다.
 */
public interface QuizQueryPort {

	List<QuizView> loadQuizzesByIds(List<Long> quizIds);

	List<QuizForGrading> loadQuizzesForGradingByIds(List<Long> quizIds);
}