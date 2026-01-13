package com.oneco.backend.StudyRecord.application.port.out;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Slice;

import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;

/**
 * StudyRecord 저장/조회 Port
 * - 애그리거트 저장소
 */
public interface StudyRecordPersistencePort {
	Optional<StudyRecord> findByMemberIdAndDailyContentId(Long memberId, Long dailyContentId);

	Optional<StudyRecord> findByIdWithAttempts(Long studyRecordId);

	StudyRecord save(StudyRecord studyRecord);

	Slice<StudyRecord> findByLastStudyRecordIdAndMemberId(
		Long memberId,
		Long lastStudyRecordId,
		LocalDate lastSubmittedDate,
		int size);

	Slice<StudyRecord> findBookmarkedByLastStudyRecordIdAndMemberId(
		Long memberId,
		Long lastStudyRecordId,
		LocalDate lastSubmittedDate,
		int size
	);
}
