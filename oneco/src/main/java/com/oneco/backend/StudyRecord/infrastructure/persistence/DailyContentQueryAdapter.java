package com.oneco.backend.StudyRecord.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.result.SubmitQuizSubmissionResult;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSnapshot;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentWithQuizzesSnapshot;
import com.oneco.backend.StudyRecord.application.port.dto.QuizSnapshot;
import com.oneco.backend.StudyRecord.application.port.out.DailyContentQueryPort;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.content.infrastructure.persistence.DailyContentJpaRepository;
import com.oneco.backend.content.domain.dailycontent.DailyContent;
import com.oneco.backend.content.domain.quiz.Quiz;
import com.oneco.backend.global.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailyContentQueryAdapter implements DailyContentQueryPort {
	private final DailyContentJpaRepository dailyContentRepository;

	@Override
	@Transactional(readOnly = true)
	public DailyContentSnapshot loadDailyContentSnapshot(Long dailyContentId) {
		DailyContent dc = dailyContentRepository.findById(dailyContentId)
			.orElseThrow(() -> BaseException.from(StudyErrorCode.DAILY_CONTENT_NOT_FOUND));

		return toDailyContentSnapshot(dc);

	}

	@Override
	@Transactional(readOnly = true)
	public List<SubmitQuizSubmissionResult.NewsItemSummary> loadNewsItemSummary(Long dailyContentId) {
		DailyContent dc = dailyContentRepository.findByIdWithNews(dailyContentId)
			.orElseThrow(() -> BaseException.from(StudyErrorCode.DAILY_CONTENT_NOT_FOUND));

		List<SubmitQuizSubmissionResult.NewsItemSummary> newsItems = dc.getNewsItems().stream()
			.map(ni -> new SubmitQuizSubmissionResult.NewsItemSummary(
				ni.getTitle(),
				ni.getWebLink().getUrl(),
				ni.getImageFile().getUrl()
			))
			.toList();
		return newsItems;
	}

	@Override
	@Transactional(readOnly = true)
	public DailyContentWithQuizzesSnapshot loadDailyContentWithQuizzes(Long dailyContentId) {
		DailyContent dc = dailyContentRepository.findByIdWithQuizzes(dailyContentId)
			.orElseThrow(() -> BaseException.from(StudyErrorCode.DAILY_CONTENT_NOT_FOUND));

		DailyContentSnapshot content = toDailyContentSnapshot(dc);
		List<QuizSnapshot> quizzes = dc.getQuizzes().stream()
			.map(this::toQuizSnapshot)
			.toList();

		return new DailyContentWithQuizzesSnapshot(content, quizzes);
	}

	private DailyContentSnapshot toDailyContentSnapshot(DailyContent dc) {
		return new DailyContentSnapshot(
			dc.getId(),
			dc.getCategoryId().getValue(),
			dc.getDaySequence().getValue(),
			dc.getDescription().getTitle(),
			dc.getDescription().getBodyText(),
			dc.getDescription().getSummary(),
			dc.getKeyword().getValue(),
			dc.getImageFile().getUrl()
		);
	}

	private QuizSnapshot toQuizSnapshot(Quiz quiz) {
		return new QuizSnapshot(
			quiz.getId(),
			quiz.getQuestion(),
			quiz.getQuestionOrder().value(),
			quiz.getOptions().getOptionTexts()
		);
	}
}
