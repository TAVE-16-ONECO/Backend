package com.oneco.backend.StudyRecord.application.port.in;

import com.oneco.backend.StudyRecord.application.dto.command.StartStudyCommand;
import com.oneco.backend.StudyRecord.application.dto.result.StartStudyResult;

public interface StartStudyUseCase {
	StartStudyResult start(StartStudyCommand command, Long memberId);
}
