package com.oneco.backend.content.domain.quiz;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class QuizOptions {

	//옵션 개수 상수
	public static final int OPTION_COUNT = 2;
	private List<QuizOption> options;

	private QuizOptions(List<QuizOption> options) {
		if (options == null || options.isEmpty()) {
			throw new IllegalArgumentException("QuizOptions는 비어 있을 수 없습니다.");
		}

		// null 원소 방지
		if (options.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("QuizOptions에 null 보기는 포함될 수 없습니다.");
		}

		// 최소 개수 규칙
		if (options.size() != OPTION_COUNT) {
			throw new IllegalArgumentException("QuizOptions는 정확히 " + OPTION_COUNT + "개여야 합니다.");
		}

		// 중복 텍스트 방지
		// 예: ["A", "B", "A"] -> distinctCount = 2, options.size() = 3 -> 예외 발생
		long distinctCount = options.stream()
			.map(QuizOption::getText)// 각 QuizOption에서 텍스트 추출
			.distinct()// 중복 제거
			.count(); // 남아있는 원소 개수

		if (distinctCount != options.size()) {
			throw new IllegalArgumentException("퀴즈 보기 텍스트는 중복될 수 없습니다.");
		}

		/**
		 * 불변성 유지 포인트
		 *
		 * List를 그대로 들고 있으면 외부에서 원본 리스트를 변경하거나,
		 * getter로 꺼낸 뒤 add/remove로 구조를 바꿀 위험이 있다.
		 *
		 * List.copyOf(options)는
		 * - 전달받은 리스트의 복사본을 만들고
		 * - 그 복사본을 수정 불가능 리스트로 만들어
		 *   "사실상 불변" 상태를 보장한다.
		 *
		 * 예를 들어 quizOptions.getOptions().add(...) 같은 시도는
		 * UnsupportedOperationException으로 막힌다.
		 *
		 * 또한 원본 리스트를 이후에 수정해도
		 * 이 객체 내부 options는 영향을 받지 않는다.
		 */
		this.options = List.copyOf(options);
	}

	public static QuizOptions of(List<QuizOption> options) {
		return new QuizOptions(options);
	}

	/**
	 * 편의 팩토리
	 * 문자열 리스트를 받아서 바로 도메인 옵션으로 변환.
	 * 서비스/컨트롤러에서 더 깔끔하게 사용 가능.
	 * 예: List<String> texts = List.of("option1", "option2");
	 * QuizOptions options = QuizOptions.ofTexts(texts);
	 * 즉 위의 코드는 다음과 같다.
	 * options = new QuizOptions(List<QuizOption.of("option1"), QuizOption.of("option2"));
	 */
	public static QuizOptions ofTexts(List<String> texts) {
		if (texts == null) {
			throw new IllegalArgumentException("texts는 null일 수 없습니다.");
		}

		List<QuizOption> list = texts.stream()
			.map(QuizOption::of)
			.collect(Collectors.toList());

		return new QuizOptions(list);
	}

}
