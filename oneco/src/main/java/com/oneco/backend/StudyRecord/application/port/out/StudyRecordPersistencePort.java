package com.oneco.backend.StudyRecord.application.port.out;

import java.util.Optional;

import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;

/**
 * StudyRecord 저장/조회 Port
 * - 애그리거트 저장소
 */
public interface StudyRecordPersistencePort {
	Optional<StudyRecord> findByMemberIdAndDailyContentId(Long memberId, Long dailyContentId);

	Optional<StudyRecord> findByIdWithAttempts(Long studyRecordId);

	StudyRecord save(StudyRecord studyRecord);
}
