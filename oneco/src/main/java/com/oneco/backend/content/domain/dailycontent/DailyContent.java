package com.oneco.backend.content.domain.dailycontent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.oneco.backend.category.domain.category.CategoryId;
import com.oneco.backend.content.domain.common.ImageFile;
import com.oneco.backend.content.domain.common.WebLink;
import com.oneco.backend.content.domain.exception.constant.ContentErrorCode;
import com.oneco.backend.content.domain.news.NewsItem;
import com.oneco.backend.content.domain.news.NewsItemOrder;
import com.oneco.backend.content.domain.quiz.AnswerIndex;
import com.oneco.backend.content.domain.quiz.QuestionOrder;
import com.oneco.backend.content.domain.quiz.Quiz;
import com.oneco.backend.content.domain.quiz.QuizOptions;
import com.oneco.backend.content.infrastructure.converter.DaySequenceConverter;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "daily_contents",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_category_day_sequence",
			columnNames = {"category_id", "day_sequence"}
		)
	}
)
public class DailyContent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private CategoryId categoryId;

	@Convert(converter = DaySequenceConverter.class)
	private DaySequence daySequence;

	@Embedded
	private ContentDescription description;

	@Embedded
	private Keyword keyword;

	@Embedded
	@AttributeOverride(name = "url", column = @Column(name = "image_url", nullable = false))
	private ImageFile imageFile;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "daily_content_id", nullable = false)
	private List<NewsItem> newsItems = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "daily_content_id", nullable = false)
	private List<Quiz> quizzes = new ArrayList<>();

	private DailyContent(
		CategoryId categoryId,
		DaySequence daySequence,
		Keyword keyword,
		ContentDescription description,
		ImageFile imageFile
	) {
		if (categoryId == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "categoryId is null");
		}
		if (daySequence == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "daySequence is null");
		}
		if (keyword == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "keyword is null");
		}
		if (description == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "description is null");
		}
		if (imageFile == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "imageFile is null");
		}

		this.categoryId = categoryId;
		this.daySequence = daySequence;
		this.keyword = keyword;
		this.description = description;
		this.imageFile = imageFile;
	}

	public static DailyContent create(
		CategoryId categoryId,
		DaySequence daySequence,
		Keyword keyword,
		ContentDescription description,
		ImageFile imageFile
	) {
		return new DailyContent(categoryId, daySequence, keyword, description, imageFile);
	}

	public void updateDescription(ContentDescription newDescription) {
		if (newDescription == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "newDescription is null");
		}
		this.description = newDescription;
	}

	public void changeSummary(String newSummary) {
		// summary의 null/blank/length 검증은 ContentDescription 내부에서 처리됨
		this.description = this.description.withSummary(newSummary);
	}

	public void changeTitle(String newTitle) {
		this.description = this.description.withTitle(newTitle);
	}

	public void changeBodyText(String newBodyText) {
		this.description = this.description.withBodyText(newBodyText);
	}

	public List<NewsItem> getNewsItems() {
		return List.copyOf(newsItems);
	}

	public void updateNewsTitle(NewsItemOrder order, String newTitle) {
		if (order == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "order is null");
		}

		NewsItem target = this.newsItems.stream()
			.filter(item -> item.getNewsItemOrder().equals(order))
			.findFirst()
			.orElseThrow(() -> BaseException.from(ContentErrorCode.NEWS_ITEM_NOT_FOUND));

		// newTitle 검증은 NewsItem.changeTitle 내부에서 처리(NEWS_TITLE_EMPTY 등)
		target.changeTitle(newTitle);
	}

	public void updateQuizQuestion(QuestionOrder order, String newQuestion) {
		if (order == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "order is null");
		}

		Quiz target = this.quizzes.stream()
			.filter(q -> q.getQuestionOrder().equals(order))
			.findFirst()
			.orElseThrow(() -> BaseException.from(ContentErrorCode.QUIZ_NOT_FOUND));

		// newQuestion 검증은 Quiz.changeQuestion 내부에서 처리(QUIZ_QUESTION_EMPTY 등)
		target.changeQuestion(newQuestion);
	}

	public NewsItem addNewsItem(
		String title,
		NewsItemOrder order,
		WebLink link,
		ImageFile imageFile
	) {
		if (title == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "title is null");
		}
		if (order == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "order is null");
		}
		if (link == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "link is null");
		}
		if (imageFile == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "imageFile is null");
		}

		validateNewsOrderDuplicate(order);

		// title blank/trim, link 규칙, imageFile 규칙 등은 NewsItem/VO 내부에서 검증
		NewsItem item = NewsItem.create(title, link, order, imageFile);
		newsItems.add(item);
		return item;
	}

	public void removeNewsItem(NewsItem item) {
		if (item == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "item is null");
		}
		newsItems.remove(item);
	}

	public void removeNewsItemByOrder(NewsItemOrder order) {
		if (order == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "order is null");
		}

		boolean removed = this.newsItems.removeIf(item -> item.getNewsItemOrder().equals(order));
		if (!removed) {
			throw BaseException.from(ContentErrorCode.NEWS_ITEM_NOT_FOUND);
		}
	}

	public void changeKeyword(Keyword newKeyword) {
		if (newKeyword == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "newKeyword is null");
		}
		this.keyword = newKeyword;
	}

	public void changeImage(ImageFile newImageFile) {
		if (newImageFile == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "newImageFile is null");
		}
		this.imageFile = newImageFile;
	}

	public boolean isSameCategory(CategoryId other) {
		if (other == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "other is null");
		}
		return this.categoryId.equals(other);
	}

	public List<Quiz> getQuizzes() {
		return List.copyOf(quizzes);
	}

	public Quiz addQuiz(
		String question,
		QuestionOrder order,
		QuizOptions options,
		AnswerIndex answerIndex
	) {
		if (question == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "question is null");
		}
		if (order == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "order is null");
		}
		if (options == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "options is null");
		}
		if (answerIndex == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "answerIndex is null");
		}

		validateQuizOrderDuplicate(order);

		// question blank, answerIndex 범위 등은 Quiz 내부에서 검증
		Quiz quiz = Quiz.create(question, order, options, answerIndex);
		quizzes.add(quiz);
		return quiz;
	}

	public void removeQuiz(Quiz quiz) {
		if (quiz == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "quiz is null");
		}
		quizzes.remove(quiz);
	}

	public void removeQuizByOrder(QuestionOrder order) {
		if (order == null) {
			throw BaseException.from(ContentErrorCode.REQUIRED_VALUE_MISSING, "order is null");
		}

		boolean removed = this.quizzes.removeIf(q -> q.getQuestionOrder().equals(order));
		if (!removed) {
			throw BaseException.from(ContentErrorCode.QUIZ_NOT_FOUND);
		}
	}

	private void validateNewsOrderDuplicate(NewsItemOrder order) {
		if (newsItems.stream().anyMatch(n -> n.getNewsItemOrder().equals(order))) {
			throw BaseException.from(ContentErrorCode.NEWS_ORDER_DUPLICATE);
		}
	}

	private void validateQuizOrderDuplicate(QuestionOrder order) {
		if (quizzes.stream().anyMatch(q -> q.getQuestionOrder().equals(order))) {
			throw BaseException.from(ContentErrorCode.QUIZ_ORDER_DUPLICATE);
		}
	}
}