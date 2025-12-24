package com.oneco.backend.StudyRecord.infrastructure.converter;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 *  Map<Long, Integer> <-> JSON String 변환기
 *
 * - DB 저장 시: Map<Long, Integer> -> JSON 문자열
 * - DB 조회 시: JSON 문자열 -> Map<Long, Integer>
 *
 * 왜 좋은가?
 * - 엔티티는 "도메인 타입(Map<Long, Integer>)"만 다룬다.
 * - JSON 직렬화/역직렬화 책임을 여기로 모아서
 * 도메인이 저장 방식에 오염되지 않는다.
 *
 * TEXT 저장보다 JSON이 나은 이유?
 * - answers는 제출값이라 데이터 구조가 더 명확하고, 실수로 깨진 문자열이 들어가면 채점 로직이 복잡해진다.
 * - JSON은 Map 구조를 명확히 표현할 수 있어, 데이터 무결성 유지에 유리하다.
 * - MySQL JSON 타입이면 DB가 유효한 JSON인지 검사해주어 안정성이 높아진다.
 */
@Converter
public class LongIntMapJsonConverter implements AttributeConverter<Map<Long, Integer>, String> {

	private static final ObjectMapper om = new ObjectMapper();
	private static final TypeReference<Map<Long, Integer>> TYPE = new TypeReference<>() {
	};

	/**
	 *  저장 시 (Java -> DB)
	 * 엔티티 상태:
	 * answers = { 101L=2, 205L=1, 309L=4 }
	 * // 의미: quizId 101번 문제는 2번 선택, 205번은 1번 선택, 309번은 4번 선택
	 *
	 * convertToDatabaseColumn() 결과(JSON 문자열):
	 * {"101":2,"205":1,"309":4}
	 *
	 * 중요한 포인트: JSON에서는 "객체의 key는 문자열"이라서
	 * Map<Long, Integer>라도 저장될 때 키가 "101"처럼 문자열 형태로 저장된다.
	 *
	 * DB 컬럼(answers)에 실제 저장되는 값:
	 * {"101":2,"205":1,"309":4}
	 * - columnDefinition이 json이면 DB가 JSON 타입으로 저장하고 유효성 검증도 한다.
	 */
	@Override
	public String convertToDatabaseColumn(Map<Long, Integer> attribute) {
		try {
			return attribute == null ? null : om.writeValueAsString(attribute);
		} catch (Exception e) {
			throw new IllegalArgumentException("answers 직렬화 실패", e);
		}
	}

	/**
	 * 조회 시 (DB -> Java)
	 * DB에서 읽은 값:
	 * dbData = {"101":2,"205":1,"309":4}
	 *
	 * convertToEntityAttribute() 결과:
	 * answers = Map<Long,Integer> { 101L=2, 205L=1, 309L=4 }
	 * - Jackson이 "문자열 키"를 Long으로 변환해서 Map<Long,...> 형태로 복원해준다.
	 */
	@Override
	public Map<Long, Integer> convertToEntityAttribute(String dbData) {
		try {
			return dbData == null ? null : om.readValue(dbData, TYPE);
		} catch (Exception e) {
			throw new IllegalArgumentException("answers 역직렬화 실패", e);
		}
	}
}