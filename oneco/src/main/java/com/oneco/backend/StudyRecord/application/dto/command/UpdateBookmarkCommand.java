package com.oneco.backend.StudyRecord.application.dto.command;

import jakarta.validation.constraints.NotNull;

public record UpdateBookmarkCommand(
	@NotNull(message = "isBookmarked는 필수입니다.")
	boolean isBookmarked,
	// path로 들어오므로 처음엔 null, 컨트롤러에서 withPath 메서드로 채워줌
	Long studyRecordId
) {
	public UpdateBookmarkCommand withPath(Long studyRecordId) {
		return new UpdateBookmarkCommand(this.isBookmarked, studyRecordId);
	}
}
