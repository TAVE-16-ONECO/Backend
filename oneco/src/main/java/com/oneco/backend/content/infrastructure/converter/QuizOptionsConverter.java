package com.oneco.backend.content.infrastructure.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneco.backend.content.domain.quiz.QuizOption;
import com.oneco.backend.content.domain.quiz.QuizOptions;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ QuizOptions <-> JSON String 변환기
 * <p>
 * - DB 저장 시: QuizOptions -> JSON 문자열
 * - DB 조회 시: JSON 문자열 -> QuizOptions
 * <p>
 * 왜 좋은가?
 * - 엔티티는 "도메인 타입(QuizOptions)"만 다룬다.
 * - JSON 직렬화/역직렬화 책임을 여기로 모아서
 * 도메인이 저장 방식에 오염되지 않는다.
 */
@Converter(autoApply = true)
public class QuizOptionsConverter implements AttributeConverter<QuizOptions, String> {

	/**
	 * ⚠️ 주의
	 * JPA Converter는 JPA가 인스턴스를 직접 만들기 때문에
	 * Spring 의존성 주입이 애매한 경우가 있다.
	 * <p>
	 * 그래서 가장 안전한 방식은
	 * ObjectMapper를 Converter 내부에서 고정해 쓰는 것이다.
	 * <p>
	 * (필요하면 모듈 등록 등 커스터마이징 가능)
	 */
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(QuizOptions attribute) {
		if (attribute == null) {
			throw new IllegalArgumentException("QuizOptions가 null일 수 없습니다.");
		}

		try {
			/**
			 *저장 포맷 선택
			 * 여기서는 DB에
			 * ["보기1","보기2","보기3"] 형태로 저장하기 위해
			 * QuizOption.text만 추출한다.
			 */
			List<String> texts = attribute.getOptions().stream()
				.map(QuizOption::getText)
				.collect(Collectors.toList());

			// List.of("보기1","보기2","보기3") -> '["보기1","보기2","보기3"]' 형태의 JSON 문자열
			return objectMapper.writeValueAsString(texts);

		} catch (Exception e) {
			throw new IllegalStateException("QuizOptions를 JSON으로 변환하는 데 실패했습니다.", e);
		}
	}

	@Override
	public QuizOptions convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			throw new IllegalArgumentException("DB 데이터가 null이거나 비어있습니다.");
		}

		try {
			/**
			 * 읽기 포맷도 저장 포맷과 맞춘다.
			 * DB에서 가져온 JSON 문자열을
			 * List<String>으로 파싱한 뒤
			 * 도메인 값 객체로 승격한다.
			 */
			// ["보기1","보기2","보기3"] 형태의 JSON 문자열 -> List<String>
			List<String> texts = objectMapper.readValue(
				dbData,
				new TypeReference<List<String>>() {
				}
			);

			// List<String> -> QuizOptions
			return QuizOptions.ofTexts(texts);

		} catch (Exception e) {
			throw new IllegalStateException("JSON을 QuizOptions로 변환하는 데 실패했습니다.", e);
		}
	}
}