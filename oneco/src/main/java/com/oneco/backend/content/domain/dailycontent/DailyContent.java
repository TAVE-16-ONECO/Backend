package com.oneco.backend.content.domain.dailycontent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.oneco.backend.content.domain.common.ImageFile;
import com.oneco.backend.content.domain.common.WebLink;
import com.oneco.backend.content.domain.news.NewsItem;
import com.oneco.backend.content.domain.news.NewsItemOrder;
import com.oneco.backend.content.domain.quiz.AnswerIndex;
import com.oneco.backend.content.domain.quiz.QuestionOrder;
import com.oneco.backend.content.domain.quiz.Quiz;
import com.oneco.backend.content.domain.quiz.QuizOptions;
import com.oneco.backend.content.infrastructure.converter.DaySequenceConverter;

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
@Table(name = "daily_contents",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_category_day_sequence",
			columnNames = {"category_id", "day_sequence"}
		)
	})
public class DailyContent {

	@Id // 기본 키
	@GeneratedValue(strategy = GenerationType.IDENTITY) // IDENTITY 전략 사용
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
	/**
	 * cascade = CascadeType.ALL:
	 * DailyContent 엔티티에 대한 모든 영속성 작업(저장, 삭제 등)이
	 * 관련된 NewsItem 엔티티에도 전파됨을 의미
	 * - 즉, 이 컬렉션 안의 자식들은 부모 생명주기에 묶어서 관리하겠다는 뜻
	 * orphanRemoval = true:
	 * DailyContent 엔티티에서 NewsItem이 제거되면
	 * 해당 NewsItem 엔티티도 자동으로 삭제됨을 의미
	 * - 즉, 부모와 연관이 끊긴 자식은 자동으로 삭제하겠다는 뜻
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "daily_content_id", nullable = false)
	private List<NewsItem> newsItems = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "daily_content_id", nullable = false)
	private List<Quiz> quizzes = new ArrayList<>();

	private DailyContent(CategoryId categoryId,
		DaySequence daySequence,
		Keyword keyword,
		ContentDescription description,
		ImageFile imageFile) {
		if (categoryId == null) {
			throw new IllegalArgumentException("categoryId는 null일 수 없습니다.");
		}
		if (daySequence == null) {
			throw new IllegalArgumentException("daySequence는 null일 수 없습니다.");
		}
		if (keyword == null) {
			throw new IllegalArgumentException("keyword는 null일 수 없습니다.");
		}
		if (description == null) {
			throw new IllegalArgumentException("description는 null일 수 없습니다.");
		}
		if (imageFile == null) {
			throw new IllegalArgumentException("imageFile는 null일 수 없습니다.");
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

	// 서비스 레이어에서 엔티티를 호출하기 전에서 검증이 끝나는 것이 안전하다. (검증은 값객체 내부)
	// 따라서 값 객체의 변수들을 받는 것이 아닌 값 객체 자체를 받도록 한다.
	// + 값 객체의 변수가 더 추가되더라도 생성자나 팩토리 메서드의 시그니처가 바뀌지 않는다.
	// 주의: 값 객체가 아닌 애그리거트 내부 엔티티는
	//      서비스가 만들어서 넘기는 것이 아닌 애그리거트 루트가 생성해서 넘겨줘야 한다.
	public void updateDescription(ContentDescription newDescription) {
		Objects.requireNonNull(newDescription, "newDescription은 null일 수 없습니다.");
		this.description = newDescription;
	}

	public void changeSummary(String newSummary) {
		this.description = this.description.withSummary(newSummary);
	}

	public void changeTitle(String newTitle) {
		this.description = this.description.withTitle(newTitle);
	}

	public void changeBodyText(String newBodyText) {
		this.description = this.description.withBodyText(newBodyText);
	}

	// 뉴스 아이템 목록을 불변 리스트로 반환한다.
	public List<NewsItem> getNewsItems() {
		return List.copyOf(newsItems);
	}

	// 예: 특정 순번의 뉴스 제목 수정
	public void updateNewsTitle(NewsItemOrder order, String newTitle) {
		NewsItem target = this.newsItems.stream()
			.filter(item -> item.getNewsItemOrder().equals(order))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("해당 순번의 뉴스가 존재하지 않습니다."));

		// NewsItem 엔티티 내부에 changeTitle 메서드가 있어야 함
		target.changeTitle(newTitle);
	}

	// 예: 특정 순번의 퀴즈 질문 수정
	public void updateQuizQuestion(QuestionOrder order, String newQuestion) {
		Quiz target = this.quizzes.stream()
			.filter(q -> q.getQuestionOrder().equals(order))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("해당 순번의 퀴즈가 존재하지 않습니다."));

		target.changeQuestion(newQuestion);
	}

	/**
	 * 뉴스 아이템을 추가한다.
	 * NewsItem은 DailyContent의 내부 엔티티이므로
	 * 애그리거트 루트인 DailyContent가 생성해서 추가해야 한다.
	 */
	public NewsItem addNewsItem(
		String title,
		NewsItemOrder order,
		WebLink link,
		ImageFile imageFile // 필요하다면 WebLink link 도 같이
	) {
		Objects.requireNonNull(title, "title은 null일 수 없다.");
		Objects.requireNonNull(order, "order는 null일 수 없다.");
		Objects.requireNonNull(imageFile, "imageFile은 null일 수 없다.");
		Objects.requireNonNull(link, "link는 null일 수 없다.");

		validateNewsOrderDuplicate(order);

		NewsItem item = NewsItem.create(title, link, order, imageFile);
		newsItems.add(item);
		return item;
	}

	public void removeNewsItem(NewsItem item) {
		if (item == null) {
			throw new IllegalArgumentException("뉴스 아이템은 null일 수 없습니다.");
		}
		newsItems.remove(item);
	}

	public void removeNewsItemByOrder(NewsItemOrder order) {
		boolean removed = this.newsItems.removeIf(item -> item.getNewsItemOrder().equals(order));
		if (!removed) {
			throw new IllegalArgumentException("삭제할 뉴스가 존재하지 않습니다.");
		}
	}

	public void changeKeyword(Keyword newKeyword) {
		Objects.requireNonNull(newKeyword, "newKeyword는 null일 수 없다.");
		this.keyword = newKeyword;
	}

	public void changeImage(ImageFile newImageFile) {
		Objects.requireNonNull(newImageFile, "newImageFile는 null일 수 없다.");
		this.imageFile = newImageFile;
	}

	public boolean isSameCategory(CategoryId other) {
		return this.categoryId.equals(other);
	}

	// 퀴즈 목록을 불변 리스트로 반환한다.
	public List<Quiz> getQuizzes() {
		return List.copyOf(quizzes);
	}

	/**
	 * 퀴즈를 추가한다.
	 * Quiz는 DailyContent의 내부 엔티티이므로
	 * 애그리거트 루트인 DailyContent가 생성해서 추가해야 한다.
	 */
	public Quiz addQuiz(
		String question,
		QuestionOrder order,
		QuizOptions options,
		AnswerIndex answerIndex
	) {
		Objects.requireNonNull(question, "question은 null일 수 없다.");
		Objects.requireNonNull(order, "order는 null일 수 없다.");
		Objects.requireNonNull(options, "options는 null일 수 없다.");
		Objects.requireNonNull(answerIndex, "answerIndex는 null일 수 없다.");

		validateQuizOrderDuplicate(order);

		Quiz quiz = Quiz.create(question, order, options, answerIndex);
		quizzes.add(quiz);
		return quiz;
	}

	public void removeQuiz(Quiz quiz) {
		if (quiz == null) {
			throw new IllegalArgumentException("퀴즈는 null일 수 없습니다.");
		}
		quizzes.remove(quiz);
	}

	public void removeQuizByOrder(QuestionOrder order) {
		boolean removed = this.quizzes.removeIf(q -> q.getQuestionOrder().equals(order));
		if (!removed) {
			throw new IllegalArgumentException("삭제할 퀴즈가 존재하지 않습니다: " + order.value());
		}
	}

	private void validateNewsOrderDuplicate(NewsItemOrder order) {
		if (newsItems.stream().anyMatch(n -> n.getNewsItemOrder().equals(order))) {
			throw new IllegalArgumentException("동일한 뉴스 순번이 이미 존재합니다: " + order.value());
		}
	}

	private void validateQuizOrderDuplicate(QuestionOrder order) {
		if (quizzes.stream().anyMatch(q -> q.getQuestionOrder().equals(order))) {
			throw new IllegalArgumentException("동일한 퀴즈 순번이 이미 존재합니다: " + order.value());
		}
	}
}