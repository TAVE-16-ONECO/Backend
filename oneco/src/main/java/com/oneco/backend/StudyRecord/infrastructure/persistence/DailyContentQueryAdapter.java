package com.oneco.backend.StudyRecord.infrastructure.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.result.NewsItemSummary;
import com.oneco.backend.StudyRecord.application.dto.result.SubmitQuizSubmissionResult;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSnapshot;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSummary;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentWithQuizzesSnapshot;
import com.oneco.backend.StudyRecord.application.port.dto.QuizSnapshot;
import com.oneco.backend.StudyRecord.application.port.out.DailyContentQueryPort;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.dailycontent.infrastructure.persistence.DailyContentJpaRepository;
import com.oneco.backend.dailycontent.domain.dailycontent.DailyContent;
import com.oneco.backend.dailycontent.domain.quiz.Quiz;
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
	public List<NewsItemSummary> loadNewsItemSummary(Long dailyContentId) {
		DailyContent dc = dailyContentRepository.findByIdWithNews(dailyContentId)
			.orElseThrow(() -> BaseException.from(StudyErrorCode.DAILY_CONTENT_NOT_FOUND));

		List<NewsItemSummary> newsItems = dc.getNewsItems().stream()
			.map(ni -> new
				NewsItemSummary(
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

	@Override
	@Transactional(readOnly = true)
	public Map<Long, DailyContentSummary> findDailyContentSummariesByIds(List<Long> dailyContentIds){
		List<DailyContent> dailyContents = dailyContentRepository.findAllWithNewsItemsByIdIn(dailyContentIds);
		Map<Long, DailyContentSummary> map = new HashMap<>();
		for(DailyContent dc:dailyContents){
			var desc = dc.getDescription();
			List<NewsItemSummary> newsSummaries = dc.getNewsItems().stream()
				.map(ni-> new NewsItemSummary(
					ni.getTitle(),
					ni.getWebLink() == null ? null : ni.getWebLink().getUrl(),
					ni.getImageFile() == null ? null : ni.getImageFile().getUrl()
				))
			.toList();

			map.put(dc.getId(), new DailyContentSummary(
				dc.getId(),
				desc.getTitle(),
				desc.getSummary(),
				newsSummaries
			));
		}

		return map;
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
