package com.oneco.backend.content.infrastructure.converter;

import com.oneco.backend.content.domain.news.NewsItemOrder;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * NewsItemOrder <-> INT 단일 컬럼 변환.
 *
 * AbstractSequence 계열 값 객체는
 * - JPA Embeddable 상속 제약 회피
 * - 도메인 불변성 유지
 * 를 위해 Converter 기반 매핑을 기본 전략으로 사용한다.
 */
@Converter(autoApply = true)
public class NewsItemOrderConverter implements AttributeConverter<NewsItemOrder,Integer> {

	@Override
	public Integer convertToDatabaseColumn(NewsItemOrder attribute){
		return attribute != null ? attribute.value(): null;
	}

	@Override
	public NewsItemOrder convertToEntityAttribute(Integer dbData){
		return dbData != null ? new NewsItemOrder(dbData): null;
	}
}
