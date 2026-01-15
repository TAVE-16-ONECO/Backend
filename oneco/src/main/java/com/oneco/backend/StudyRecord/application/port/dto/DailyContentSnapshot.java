package com.oneco.backend.StudyRecord.application.port.dto;

public record DailyContentSnapshot(
	Long dailyContentId,
	Long categoryId,
	int daySequence,
	String title,
	String bodyText,
	String summary,
	String keyword,
	String imageUrl
) { } 