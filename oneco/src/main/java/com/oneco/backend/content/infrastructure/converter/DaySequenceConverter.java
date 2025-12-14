package com.oneco.backend.content.infrastructure.converter;

import com.oneco.backend.content.domain.dailycontent.DaySequence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DaySequenceConverter implements AttributeConverter<DaySequence, Integer> {
	@Override
	public Integer convertToDatabaseColumn(DaySequence attribute) {
		return attribute != null ? attribute.value() : null;
	}

	@Override
	public DaySequence convertToEntityAttribute(Integer dbData) {
		return dbData != null ? new DaySequence(dbData) : null;
	}
}
