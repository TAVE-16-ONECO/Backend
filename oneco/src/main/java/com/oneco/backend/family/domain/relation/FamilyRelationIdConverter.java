package com.oneco.backend.family.domain.relation;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * DB의 Long 타입과 FamilyRelationId 타입 간의 변환을 담당하는 JPA 컨버터
 */
@Converter(autoApply = true) // autoApply를 켜면 모든 FamilyRelationId 필드에 자동 적용
public class FamilyRelationIdConverter implements AttributeConverter<FamilyRelationId, Long> {

	// FamilyRelationId -> Long 변환
	@Override
	public Long convertToDatabaseColumn(FamilyRelationId attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.getValue();
	}

	// Long -> FamilyRelationId 변환
	@Override
	public FamilyRelationId convertToEntityAttribute(Long dbData) {
		if (dbData == null) {
			return null;
		}
		return FamilyRelationId.of(dbData);
	}
}