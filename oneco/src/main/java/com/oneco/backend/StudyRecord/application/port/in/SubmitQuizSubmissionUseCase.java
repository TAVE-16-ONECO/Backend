package com.oneco.backend.StudyRecord.application.port.in;

import com.oneco.backend.StudyRecord.application.dto.command.SubmitQuizSubmissionCommand;
import com.oneco.backend.StudyRecord.application.dto.result.SubmitQuizSubmissionResult;

public interface SubmitQuizSubmissionUseCase {
	SubmitQuizSubmissionResult submit(SubmitQuizSubmissionCommand command, Long memberId);
}
