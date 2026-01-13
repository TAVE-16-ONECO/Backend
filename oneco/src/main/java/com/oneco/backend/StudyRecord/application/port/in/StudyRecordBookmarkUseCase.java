package com.oneco.backend.StudyRecord.application.port.in;

import com.oneco.backend.StudyRecord.application.dto.command.UpdateBookmarkCommand;
import com.oneco.backend.global.security.jwt.JwtPrincipal;

public interface StudyRecordBookmarkUseCase {
	public void updateBookmark(UpdateBookmarkCommand command, JwtPrincipal principal);
}
