package com.oneco.backend.content.infrastructure.converter;

import com.oneco.backend.content.domain.quiz.QuestionOrder;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class QuestionOrderConverter implements AttributeConverter<QuestionOrder, Integer> {

	@Override
	public Integer convertToDatabaseColumn(QuestionOrder attribute){
		return attribute != null ? attribute.value(): null;
	}

	@Override
	public QuestionOrder convertToEntityAttribute(Integer dbData){
		return dbData != null ? new QuestionOrder(dbData): null;
	}
}

