package com.oneco.backend.StudyRecord.domain.studyRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oneco.backend.StudyRecord.domain.quizAttempt.AttemptNo;
import com.oneco.backend.StudyRecord.domain.quizAttempt.AttemptResult;
import com.oneco.backend.StudyRecord.domain.quizAttempt.CorrectCount;
import com.oneco.backend.StudyRecord.domain.quizAttempt.QuizAttempt;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.category.domain.category.CategoryId;
import com.oneco.backend.dailycontent.domain.dailycontent.DailyContentId;
import com.oneco.backend.global.entity.BaseTimeEntity;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.MemberId;
import com.oneco.backend.mission.domain.mission.MissionId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
	name = "study_records",
	uniqueConstraints = {
		// 같은 멤버가 같은 dailyContent에 대해 기록은 1개만
		@UniqueConstraint(name = "uk_member_daily", columnNames = {"member_id", "daily_content_id"})
	},
	indexes = {
		@Index(name ="idx_member_created_id",columnList = "member_id, created_at, id")
	}

)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyRecord extends BaseTimeEntity {

	private static final int MAX_ATTEMPTS = 2;
	private static final int DEFAULT_RETRY = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "mission_id", nullable = false))
	private MissionId missionId;

	@Column(name = "is_bookmarked", nullable = false)
	private boolean bookmarked = false;

	@Column(name="quiz_submitted_date", nullable = true)
	private LocalDate quizSubmittedDate; // null = 아직 제출 안 함

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "member_id", nullable = false))
	private MemberId memberId;

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "category_id", nullable = false))
	private CategoryId categoryId;

	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "daily_content_id", nullable = false))
	private DailyContentId dailyContentId;

	// 퀴즈 진행 상태
	@Enumerated(EnumType.STRING)
	@Column(name = "quiz_status", nullable = false, length = 50)
	private QuizProgressStatus quizProgressStatus = QuizProgressStatus.READY;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_record_id", nullable = false) // quiz_attempts에 FK 생성
	@OrderBy("attemptNo.value ASC")
	private List<QuizAttempt> attempts = new ArrayList<>();

	/**
	 * 뉴스 언락 여부
	 * - PASS이면 true
	 * - FAIL(기회 끝)도 true
	 */
	@Column(name = "news_unlocked", nullable = false)
	private boolean newsUnlocked;

	private StudyRecord(
		MissionId missionId,
		MemberId memberId,
		CategoryId categoryId,
		DailyContentId dailyContentId
	) {
		this.missionId = missionId;
		this.memberId = memberId;
		this.categoryId = categoryId;
		this.dailyContentId = dailyContentId;
	}

	/**
	 * "학습하기" or "마스터하기" 눌러 본문을 열었다는 상태 갱신
	 * 여기서 StudyRecord 생성
	 */
	public static StudyRecord openStudy(
		MissionId missionId,
		MemberId memberId,
		CategoryId categoryId,
		DailyContentId dailyContentId
	) {
		StudyRecord sr = new StudyRecord(
			missionId,
			memberId,
			categoryId,
			dailyContentId
		);

		sr.newsUnlocked = false;
		return sr;
	}

	// =========================
	// 퀴즈 도전하기 버튼 누름/ 기회 1번 더
	// 퀴즈 시작: 1차/2차 생성 (총 2회 제한)
	// =========================
	public QuizAttempt startQuizAttempt(List<Long> quizIds) {

		// 이미 진행 중이면 중복 시작 불가
		if (quizProgressStatus == QuizProgressStatus.IN_PROGRESS) {
			throw BaseException.from(StudyErrorCode.QUIZ_ALREADY_STARTED);
		}

		// 이미 PASS/FAIL로 종료된 학습이면 시작 불가
		if (quizProgressStatus == QuizProgressStatus.PASSED || quizProgressStatus == QuizProgressStatus.FAILED) {
			throw BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS);
		}

		// 총 2회 제한
		if (attempts.size() >= 2) {
			throw BaseException.from(StudyErrorCode.QUIZ_ATTEMPT_EXCEEDED);
		}

		// 2차 시작 조건: 1차가 SUBMITTED + FAIL 이어야 함
		if (attempts.size() == 1) {
			QuizAttempt first = attempts.get(0);
			if (quizProgressStatus != QuizProgressStatus.RETRY_AVAILABLE) {
				throw BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS);
			}
			// 방어적으로 확인
			if (first.getAttemptResult() != AttemptResult.FAIL) {
				throw BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS);
			}
		}

		// 시도 번호 생성 및 시도 시작
		AttemptNo attemptNo = AttemptNo.of(attempts.size() + 1);
		QuizAttempt newAttempt = QuizAttempt.start(attemptNo, quizIds);
		attempts.add(newAttempt);

		quizProgressStatus = QuizProgressStatus.IN_PROGRESS;
		return newAttempt;
	}

	// =========================
	// 퀴즈 제출: 1차 FAIL이면 재시도 열어주고, 2차 FAIL이면 종료
	// =========================
	public void submitQuizAttempt(Long attemptId,
		Map<Long, Integer> answers,
		CorrectCount correctCount) {

		QuizAttempt attempt = findAttemptOrThrow(attemptId);

		// 자식 엔티티가 “제출 가능 상태/quizId mismatch/옵션 인덱스” 등 검증 + result 계산까지 수행
		attempt.submit(answers, correctCount);

		// 루트가 “전체 진행 상태”를 업데이트 (총 2회 규칙 적용)
		if (attempt.getAttemptResult() == AttemptResult.PASS) {
			quizProgressStatus = QuizProgressStatus.PASSED;
			newsUnlocked = true;
			return;
		}

		// 퀴즈 1번이라도 제출하면 날짜 기록
		this.quizSubmittedDate = LocalDate.now();
		// FAIL인 경우
		if (attempt.getAttemptNo().isFirst()) {
			//  1차 FAIL → 2차 기회 열어줌
			quizProgressStatus = QuizProgressStatus.RETRY_AVAILABLE;
		} else if (attempt.getAttemptNo().isSecond()) {
			//  2차 FAIL → 종료
			quizProgressStatus = QuizProgressStatus.FAILED;
			newsUnlocked = true;
		}
	}

	public void setBookmarked(boolean bookmarked) {
		this.bookmarked= bookmarked;
	}

	private QuizAttempt findAttemptOrThrow(Long attemptId) {
		if (attemptId == null) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}
		return attempts.stream()
			.filter(a -> attemptId.equals(a.getId()))
			.findFirst()
			.orElseThrow(() -> BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS));
	}
}

