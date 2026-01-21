package com.oneco.backend.StudyRecord.application.port.in;

import com.oneco.backend.StudyRecord.application.dto.command.StartStudyCommand;
import com.oneco.backend.StudyRecord.application.dto.result.StartStudyResult;
import com.oneco.backend.member.domain.FamilyRole;

public interface StartStudyUseCase {
	StartStudyResult start(StartStudyCommand command, Long memberId, FamilyRole familyRole);
}
