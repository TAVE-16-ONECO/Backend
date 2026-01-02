package com.oneco.backend.StudyRecord.application.port.dto;

import java.util.List;

public 	record DailyContentWithQuizzesSnapshot(
	DailyContentSnapshot content,
	List<QuizSnapshot> quizzes
) { }
