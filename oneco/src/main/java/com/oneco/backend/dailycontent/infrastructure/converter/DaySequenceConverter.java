package com.oneco.backend.dailycontent.infrastructure.converter;

import com.oneco.backend.dailycontent.domain.dailycontent.DaySequence;

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
