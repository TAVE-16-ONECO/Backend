package com.oneco.backend.StudyRecord.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.command.UpdateBookmarkCommand;
import com.oneco.backend.StudyRecord.application.port.in.StudyRecordBookmarkUseCase;
import com.oneco.backend.StudyRecord.application.port.out.StudyRecordPersistencePort;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.security.jwt.JwtPrincipal;
import com.oneco.backend.member.domain.FamilyRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyRecordBookmarkService implements StudyRecordBookmarkUseCase {

	private final StudyRecordPersistencePort studyRecordPersistencePort;

	/**
	 * 북마크 업데이트 서비스
	 * - 1. principal에서 역할 검증( 부모는 북마크 기능 불가능)
	 * - 2. StudyRecordId로 StudyRecord 조회
	 * - 3. 조회된 StudyRecord의 memberId와 토큰에서 추출된 memberId 같은지 비교
	 * - 4. 현재 studyRecord의 bookmarked 변수와 입력으로 받은 isBookmarked가 다른지 확인
	 * - 5. studyRecord.updateBookmark() 도메인 메서드로 북마크 업데이트
	 * - 6. void 리턴
	 */
	@Override
	@Transactional
	public void updateBookmark(UpdateBookmarkCommand command, JwtPrincipal principal) {
		FamilyRole familyRole = FamilyRole.parseRole(principal.familyRole());

		if (familyRole != FamilyRole.CHILD) {
			throw BaseException.from(StudyErrorCode.PARENT_CANNOT_BOOKMARK);
		}
		StudyRecord studyRecord = studyRecordPersistencePort
			.findByIdWithAttempts(command.studyRecordId())
			.orElseThrow(() -> BaseException.from(StudyErrorCode.STUDY_RECORD_NOT_FOUND));
		if (!studyRecord.getMemberId().getValue().equals(principal.memberId())) {
			throw BaseException.from(StudyErrorCode.STUDY_RECORD_FORBIDDEN);
		}
		if (studyRecord.isBookmarked() == command.isBookmarked()) {
			return;
		}
		studyRecord.updateBookmarked(command.isBookmarked());

		return;
	}


}
