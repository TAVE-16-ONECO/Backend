package com.oneco.backend.StudyRecord.domain.QuizAttempt;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.StudyRecord.infrastructure.converter.LongIntMapJsonConverter;
import com.oneco.backend.StudyRecord.infrastructure.converter.LongListJsonConverter;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
	name = "quiz_attempt",
	uniqueConstraints = {
		// 같은 학습 기록에서 동일한 시도 번호가 중복되지 않도록 설정
		@UniqueConstraint(name = "uk_quiz_attempt_unique", columnNames = {"attemp_no", "study_record_id"})
	}
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 맞춘 개수 (0~3)
	 */
	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "correct_count"))
	private CorrectCount correctCount;

	/**
	 * PASS / FAIL
	 * 제출 전에는 null
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "result", length = 20)
	private AttemptResult attemptResult;

	/**
	 * 시도 상태 (SUBMITTED / IN_PROGRESS)
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "attemptStatus", nullable = false, length = 20)
	private AttemptStatus attemptStatus;

	/**
	 * 몇 번째 시도인지 나타내는 번호 (1부터 시작)
	 */
	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "attemp_no", nullable = false))
	private AttemptNo attemptNo;

	/**
	 * 이 attemp에서 출제된 문제 3개를 저장
	 * - 제출 시 클라가 임의의 quizId를 보내올 수 있기 때문에
	 * - DB에 저장된 문제와 비교 검증이 필요
	 */
	@Convert(converter = LongListJsonConverter.class)
	@Column(name = " quiz_ids", nullable = false, columnDefinition = "TEXT")
	private List<Long> quizIds;

	/**
	 * 제출된 답안 (quizId -> selectedIndex)
	 * - 제출전에는 null일 수 있음
	 */
	@Convert(converter = LongIntMapJsonConverter.class)
	@Column(name = "answers", columnDefinition = "json")
	private Map<Long, Integer> answers;

	// =========================
	// 생성: 시작 시점
	// =========================
	public static QuizAttempt start(AttemptNo attemptNo, List<Long> quizIds) {

		// 1) 시도 번호 필수
		if (attemptNo == null) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}

		// 2) 출제 문제 3개 필수
		if (quizIds == null) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}
		if (quizIds.size() != 3) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}

		// 3) 중복 출제 방지 (101,101,205 같은 케이스 차단)
		if (quizIds.stream().distinct().count() != 3) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}
		QuizAttempt a = new QuizAttempt();
		a.attemptStatus = AttemptStatus.IN_PROGRESS;
		a.attemptNo = attemptNo;
		a.quizIds = List.copyOf(quizIds); // 외부에서 리스트 수정 못 하게 방어적 복사

		// 제출 전 상태: answers/correctCount/result는 비어있음
		a.answers = null;
		a.correctCount = null;
		a.attemptResult = null;

		return a;
	}

	// =========================
	// 상태 전이: 제출 시점
	// =========================
	public void submit(Map<Long, Integer> answers, CorrectCount correctCount) {

		// 1) 제출 가능한 상태인지 확인
		// - 이미 제출된 시도면 "이미 제출됨" 예외
		if (this.attemptStatus == AttemptStatus.SUBMITTED) {
			throw BaseException.from(StudyErrorCode.QUIZ_ALREADY_SUBMITTED);
		}
		// - 그 외(IN_PROGRESS가 아닌 모든 상태: EXPIRED 등)는 "상태 전이 불가"
		if (this.attemptStatus != AttemptStatus.IN_PROGRESS) {
			throw BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS);
		}

		// 2) 제출 데이터 필수
		if (answers == null || answers.isEmpty()) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}
		if (correctCount == null) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}

		// 3) answers 자체에 null key/value가 있으면 "제출 데이터 이상"
		for (Map.Entry<Long, Integer> e : answers.entrySet()) {
			if (e.getKey() == null) {
				throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
			}
			if (e.getValue() == null) {
				throw BaseException.from(StudyErrorCode.INVALID_OPTION_INDEX);
			}
		}

		// 4) "출제된 문제 3개"와 "제출된 문제 3개"가 정확히 일치해야 함
		// - contains 체크만 하면 2개만 제출 같은 케이스를 놓치거나, 일부만 맞춰서 조작 가능
		// - issued == submitted 집합 동일성 비교가 더 안전
		if (answers.size() != this.quizIds.size()) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}

		// 출제된 문제 집합 vs 제출된 문제 집합
		// - Set으로 만들어서 비교
		// keySet()는 Map에서 키들만 뽑아내서 Set으로 만들어줌
		// - Set으로 만드는 이유
		//  -> List는 순서가 다르면 동일성 비교에서 false가 나오기 때문
		//   -> Set은 순서 상관없이 동일한 원소만 있으면 동일성 비교에서 true가 나오기 때문
		//  -> 예: 출제 [101,205,309] / 제출 [309,205,101] 같은 케이스 허용
		Set<Long> issued = new HashSet<>(this.quizIds);     // 서버가 출제한 quizIds(3개)
		Set<Long> submitted = new HashSet<>(answers.keySet());             // 클라가 제출한 quizIds
		if (!issued.equals(submitted)) {
			// 예: 출제 [101,205,309] / 제출 [101,205,777] 또는 [101,205] 등
			throw BaseException.from(StudyErrorCode.QUIZ_ID_MISMATCH);
		}

		// 5) 상태 반영
		this.answers = Map.copyOf(answers); // 방어적 복사
		this.correctCount = correctCount;
		this.attemptStatus = AttemptStatus.SUBMITTED;
	}

	public boolean isSubmitted() {
		return this.attemptStatus == AttemptStatus.SUBMITTED;
	}

	/**
	 * 3문제 모두 정답이면 PASS
	 */
	public boolean isPassed() {
		return isSubmitted() && this.correctCount != null && this.correctCount.isPerfect();
	}

	// =========================
	// 상태 전이: 만료 처리
	// =========================
	public void expire() {
		// IN_PROGRESS 상태에서만 만료로 전이
		if (this.attemptStatus == AttemptStatus.IN_PROGRESS) {
			this.attemptStatus = AttemptStatus.EXPIRED;
		}
	}
}
