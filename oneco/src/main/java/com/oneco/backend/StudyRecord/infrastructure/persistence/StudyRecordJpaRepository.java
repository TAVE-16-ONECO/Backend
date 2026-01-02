package com.oneco.backend.StudyRecord.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;

public interface StudyRecordJpaRepository extends JpaRepository<StudyRecord, Long> {

	// memberId.value 와 dailyContentId.value 로 기존 기록 조회
	Optional<StudyRecord> findByMemberId_ValueAndDailyContentId_Value(Long memberId, Long dailyContentId);

	@Query("""
		select sr from StudyRecord sr
		left join fetch sr.attempts a
		where sr.id = :id
		""")
	Optional<StudyRecord> findByIdWithAttempts(@Param("id") Long id);

}

