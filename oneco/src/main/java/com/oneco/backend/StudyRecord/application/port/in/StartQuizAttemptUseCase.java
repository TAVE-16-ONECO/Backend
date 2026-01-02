package com.oneco.backend.StudyRecord.application.port.in;

import com.oneco.backend.StudyRecord.application.dto.command.StartQuizAttemptCommand;
import com.oneco.backend.StudyRecord.application.dto.result.StartQuizAttemptResult;

public interface StartQuizAttemptUseCase {
	StartQuizAttemptResult start(StartQuizAttemptCommand command, Long memberId);
}
