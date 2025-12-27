package com.oneco.backend.StudyRecord.infrastructure.converter;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * TEXT로 저장된 JSON 문자열 <-> List<Long> 변환기
 * TEXT로 저장하는 이유
 * - attempt 단건 조회하면 quizIds는 서버가 그냥 "이 attempt에 해당하는 quizId 목록"을
 * DB에서 한 번에 꺼내서 List<Long>으로 만든 후
 * 애플리케이션에서 비교 검증만 하면 됨
 * - DB에서 quizIds안에 205가 포함된 attempt를 찾는 쿼리는 안 짤 거라서
 * JSON 배열을 TEXT로 저장해도 무방함
 */
@Converter
public class LongListJsonConverter implements AttributeConverter<List<Long>, String> {

	// ObjectMapper는 스레드 안전하므로 하나만 만들어서 재사용해도 됨
	// ObjectMapper는 JSON 직렬화/역직렬화에 특화된 라이브러리
	// om.writeValueAsString(...)  -> Java 객체를 JSON 문자열로 변환
	// om.readValue(..., TYPE)     -> JSON 문자열을 Java 객체로 변환
	private static final ObjectMapper om = new ObjectMapper();
	// 역직렬화할 때 정확히 어떤 타입으로 읽을지 알려주기 위한 TypeReference
	// 여기서는 List<Long> 타입 지정
	// TypeReference는 제네릭 타입 정보를 유지하면서 Jackson이 타입을 인식하게 도와줌
	private static final TypeReference<List<Long>> TYPE = new TypeReference<>() {
	};

	/**
	 * [Java -> DB] 저장/업데이트 시 호출
	 * <p>
	 * 예)
	 * attribute = List.of(101L, 205L, 309L)
	 * return    = "[101,205,309]"
	 * <p>
	 * 최종적으로 DB 컬럼에는 위 JSON 문자열이 저장된다.
	 */
	@Override
	public String convertToDatabaseColumn(List<Long> attribute) {
		try {
			// attribute가 null이면 null 저장
			// (nullable=false 컬럼이면 여기서 null 나가면 DB 제약조건 위반 가능)
			return attribute == null ? null : om.writeValueAsString(attribute);
		} catch (Exception e) {
			// 컨버터 에러는 데이터 포맷/서버 내부 문제 성격 -> 런타임 예외로 올려서 트랜잭션 롤백 유도
			throw new IllegalArgumentException("quizIds 직렬화 실패", e);
		}
	}

	/**
	 * [DB -> Java] 조회 시 호출
	 * <p>
	 * 예)
	 * dbData = "[101,205,309]"   // DB에 저장된 문자열
	 * return = List.of(101L, 205L, 309L)
	 * <p>
	 * dbData가 null이면 엔티티에서는 빈 리스트(List.of())로 취급한다.
	 */
	@Override
	public List<Long> convertToEntityAttribute(String dbData) {
		try {
			// DB에 null이 들어있으면 엔티티에선 빈 리스트로 보이게 함
			// (정책적으로 "없는 값"을 null보다 []로 다루고 싶을 때 유용)
			return dbData == null ? List.of() : om.readValue(dbData, TYPE);
		} catch (Exception e) {
			// DB에 깨진 JSON이 저장돼 있거나, 타입이 안 맞으면 여기서 터짐
			throw new IllegalArgumentException("quizIds 역직렬화 실패", e);
		}
	}
}
