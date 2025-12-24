package com.oneco.backend.member.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true) // 모든 MemberId 타입에 자동 적용
public class MemberIdConverter implements AttributeConverter<MemberId, Long> {

	@Override
	public Long convertToDatabaseColumn(MemberId attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.getValue();
	}

	@Override
	public MemberId convertToEntityAttribute(Long dbData) {
		if (dbData == null) {
			return null;
		}
		return MemberId.of(dbData);
	}
}
