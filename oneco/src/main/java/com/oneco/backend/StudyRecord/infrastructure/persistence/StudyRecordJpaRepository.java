package com.oneco.backend.StudyRecord.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oneco.backend.StudyRecord.domain.studyRecord.QuizProgressStatus;
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

	@Query("""
			select sr from StudyRecord sr
			where sr.memberId.value = :memberId
			and sr.missionId.value = :missionId
			and sr.categoryId.value = :categoryId
		""")
	List<StudyRecord> findByMemberIdAndMissionIdAndCategoryId(
		@Param("memberId") Long memberId,
		@Param("missionId") Long missionId,
		@Param("categoryId") Long categoryId);

	@Query("""
			select sr 
			from StudyRecord sr
			where sr.memberId.value = :memberId
			and sr.quizProgressStatus in :statuses
			and (
				:lastSubmittedDate is null
				or sr.quizSubmittedDate < :lastSubmittedDate
				or (sr.quizSubmittedDate = :lastSubmittedDate and sr.id<:lastStudyRecordId)
			)
			order by sr.quizSubmittedDate desc, sr.id desc
		""")
	Slice<StudyRecord> findByLastStudyRecordIdAndMemberId(
		@Param("memberId") Long memberId,
		@Param("lastStudyRecordId") Long lastStudyRecordId,
		@Param("lastSubmittedDate") LocalDate lastSubmittedDate,
		@Param("statuses") List<QuizProgressStatus> statuses,
		Pageable pageable
	);

	@Query("""
			select sr
				from StudyRecord sr
				where sr.bookmarked= :isBookmarked
				and sr.memberId.value = :memberId
				and sr.quizProgressStatus in :statuses
				and (
					:lastSubmittedDate is null
					or sr.quizSubmittedDate < :lastSubmittedDate
					or (sr.quizSubmittedDate=:lastSubmittedDate and sr.id < :lastStudyRecordId)
				)
				order by sr.quizSubmittedDate desc, sr.id desc
		""")
	Slice<StudyRecord> findBookmarkedByLastStudyRecordIdAndMemberId(
		@Param("memberId") Long memberId,
		@Param("lastStudyRecordId") Long lastStudyRecordId,
		@Param("lastSubmittedDate") LocalDate lastSubmittedDate,
		@Param("statuses") List<QuizProgressStatus> statuses,
		@Param("isBookmarked") boolean isBookmarked,
		Pageable pageable
	);
}

