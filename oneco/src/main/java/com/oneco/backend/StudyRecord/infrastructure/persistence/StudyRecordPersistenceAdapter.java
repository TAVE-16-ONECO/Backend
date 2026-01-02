package com.oneco.backend.StudyRecord.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.port.out.StudyRecordPersistencePort;
import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StudyRecordPersistenceAdapter implements StudyRecordPersistencePort {
	private final StudyRecordJpaRepository studyRecordJpaRepository;

	@Override
	@Transactional(readOnly = true)
	public Optional<StudyRecord> findByMemberIdAndDailyContentId(Long memberId, Long dailyContentId) {
		return studyRecordJpaRepository.findByMemberId_ValueAndDailyContentId_Value(memberId, dailyContentId);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StudyRecord> findByIdWithAttempts(Long studyRecordId) {
		return studyRecordJpaRepository.findByIdWithAttempts(studyRecordId);
	}

	@Override
	@Transactional
	public StudyRecord save(StudyRecord studyRecord) {

		StudyRecord sr= studyRecordJpaRepository.save(studyRecord);
		studyRecordJpaRepository.flush();
		return sr;
	}

}
