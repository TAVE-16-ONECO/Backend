package com.oneco.backend.StudyRecord.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.command.SubmitQuizSubmissionCommand;
import com.oneco.backend.StudyRecord.application.dto.result.AttemptSummary;
import com.oneco.backend.StudyRecord.application.dto.result.NewsItemSummary;
import com.oneco.backend.StudyRecord.application.dto.result.SubmitQuizSubmissionResult;
import com.oneco.backend.StudyRecord.application.port.dto.QuizForGrading;
import com.oneco.backend.StudyRecord.application.port.in.SubmitQuizSubmissionUseCase;
import com.oneco.backend.StudyRecord.application.port.out.DailyContentQueryPort;
import com.oneco.backend.StudyRecord.application.port.out.QuizQueryPort;
import com.oneco.backend.StudyRecord.application.port.out.StudyRecordPersistencePort;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.StudyRecord.domain.quizAttempt.CorrectCount;
import com.oneco.backend.StudyRecord.domain.quizAttempt.QuizAttempt;
import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;
import com.oneco.backend.global.exception.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitQuizSubmissionService implements SubmitQuizSubmissionUseCase {

	private final StudyRecordPersistencePort studyRecordPersistencePort;
	private final QuizQueryPort quizQueryPort;
	private final DailyContentQueryPort dailyContentQueryPort;

	@Override
	@Transactional
	public SubmitQuizSubmissionResult submit(SubmitQuizSubmissionCommand command, Long memberId) {
		Long studyRecordId = command.studyRecordId();
		Long attemptId = command.attemptId();
		// 제출 답안
		// 예시 : { 1001: 1, 1002: 0, 1003: 2 }
		Map<Long, Integer> answers = command.answers();
		log.info("제출 답안 받음: studyRecordId={}, attemptId={}, answers={}", studyRecordId, attemptId, answers);

		// StudyRecord 조회 및 검증
		StudyRecord sr = studyRecordPersistencePort.findByIdWithAttempts(studyRecordId)
			.orElseThrow(() -> BaseException.from(StudyErrorCode.STUDY_RECORD_NOT_FOUND));
		log.info("학습기록 조회 완료: {}", sr);
		log.info("학습기록 멤버ID: {}, 요청 멤버ID: {}", sr.getMemberId().getValue(), memberId);

		// 멤버 권한 검증
		if (!sr.getMemberId().getValue().equals(memberId)) {
			throw BaseException.from(StudyErrorCode.STUDY_RECORD_FORBIDDEN);
		}
		log.info("멤버 권한 검증 통과: memberId={}는 studyRecordId={}에 대한 권한 보유", memberId, studyRecordId);

		// QuizAttempt 조회 및 검증
		QuizAttempt attempt = sr.getAttempts().stream()
			.filter(a -> attemptId.equals(a.getId()))
			.findFirst()
			.orElseThrow(() -> BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "attempt not found"));
		log.info("퀴즈 시도 조회 완료: {}", attempt);

		List<Long> quizIds = attempt.getQuizIds();

		List<QuizForGrading> quizzes = quizQueryPort.loadQuizzesForGradingByIds(quizIds);
		log.info("채점용 퀴즈 조회 완료: quizzes={}", quizzes);

		// 누락 검증
		if (quizzes.size() != quizIds.size()) {
			throw BaseException.from(StudyErrorCode.QUIZ_NOT_FOUND);
		}
		log.info("누락 검증 통과: 발급된 퀴즈 개수={} == 조회된 퀴즈 개수={}", quizIds.size(), quizzes.size());

		// 채점 전 검증 수행
		if (answers == null || answers.isEmpty()) {
			throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION);
		}

		// (2) answers.keySet과 quizIds 집합 동일성 체크
		// (예시 : [101, 102, 103]/[101, 102, 103]  OK ,
		//        [101, 102]/[101, 102, 103]  X , [101, 102, 104]/[101, 102, 103]  X)
		Set<Long> issued = new HashSet<>(quizIds); //서버가 발급한 퀴즈 ID 집합
		Set<Long> submitted = new HashSet<>(answers.keySet()); // 사용자가 제출한 퀴즈 ID 집합
		log.info("발급된 퀴즈 ID 집합: {}", issued);
		log.info("제출된 퀴즈 ID 집합: {}", submitted);

		if (!issued.equals(submitted)) {
			throw BaseException.from(StudyErrorCode.QUIZ_ID_MISMATCH);
		}
		log.info("퀴즈 ID 집합 동일성 검증 통과");

		// (3) 보기 인덱스 범위 검증: 0 <= selectedIndex < optionsSize

		// quizzes를 map으로 만들어 quizId -> quiz 정보 빠르게 접근
		// Function.identity()는 자기 자신을 반환하는 함수 (QuizView 객체 자체를 반환)
		// 101 -> QuizView(id=101, options=[...], ...)
		Map<Long, QuizForGrading> quizMap = quizzes.stream()
			.collect(Collectors.toMap(QuizForGrading::quizId, Function.identity()));
		log.info("퀴즈 맵 생성 완료: quizMap={}", quizMap);

		for (Long quizId : quizIds) {
			// 제출된 답안에서 선택된 보기 인덱스 가져오기
			// 예: quizId=1001 -> selected=1
			Integer selected = answers.get(quizId);
			if (selected == null) {
				throw BaseException.from(StudyErrorCode.INVALID_QUIZ_SUBMISSION, "selected is null. quizId=" + quizId);
			}
			log.info("퀴즈 ID={}에 대한 선택된 보기 인덱스: selected={}", quizId, selected);

			// 보기 인덱스가 음수인 경우
			if (selected < 0) {
				throw BaseException.from(StudyErrorCode.INVALID_OPTION_INDEX, "negative index. quizId=" + quizId);
			}
			log.info("보기 인덱스 음수 검증 통과: quizId={}, selected={}", quizId, selected);
			// 해당 quizId의 QuizView에서 options 크기 가져오기
			QuizForGrading q = quizMap.get(quizId);
			int optionsSize = (q.options() == null) ? 0 : q.options().size();
			log.info("퀴즈 ID={}에 대한 보기 개수: optionsSize={}", quizId, optionsSize);

			// optionsSize가 0이면 데이터 자체가 이상한 것이므로 예외 처리
			if (optionsSize <= 0) {
				throw BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "options is empty. quizId=" + quizId);
			}

			// 선택된 인덱스가 optionsSize 범위를 벗어나는지 검사
			// 예: optionsSize=4 인데 selected=4 또는 selected=5 등
			if (selected >= optionsSize) {
				throw BaseException.from(StudyErrorCode.INVALID_OPTION_INDEX,
					"out of range. quizId=" + quizId + ", selected=" + selected + ", optionsSize=" + optionsSize);
			}
			log.info("보기 인덱스 범위 검증 통과: quizId={}, selected={}, optionsSize={}", quizId, selected, optionsSize);
		}
		// 정답 비교로 correctCount 계산
		int correct = 0;
		for (Long quizId : quizIds) {
			QuizForGrading q = quizMap.get(quizId);

			Integer selected = answers.get(quizId);
			Integer answerIndex = q.correctIndex();

			// answerIndex가 null이면 콘텐츠 데이터 문제
			if (answerIndex == null) {
				throw BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "answerIndex is null. quizId=" + quizId);
			}

			if (selected.equals(answerIndex)) {
				correct++;
			}
		}
		log.info("정답 비교 완료: correctCount={}", correct);

		CorrectCount correctCount = CorrectCount.of(correct);

		// 도메인 호출 : 제출 + 상태 전이 (1차 FAIL이면 RETRY_AVAILABLE, 2차 FAIL이면 FAILED, 3문제 다 맞으면 PASSED)
		sr.submitQuizAttempt(attemptId, answers, correctCount);
		log.info("도메인 제출 처리 완료: {}", sr);

		// 8) 저장
		StudyRecord saved = studyRecordPersistencePort.save(sr);
		log.info("학습기록 저장 완료: {}", saved);

		// 9) 응답 구성
		QuizAttempt updatedAttempt = saved.getAttempts().stream()
			.filter(a -> attemptId.equals(a.getId()))
			.findFirst()
			.orElseThrow(() -> BaseException.from(StudyErrorCode.INVALID_STUDY_STATUS, "attempt not found after save"));
		log.info("저장 후 업데이트된 퀴즈 시도 조회 완료: {}", updatedAttempt);

		int attemptNo = updatedAttempt.getAttemptNo().getValue();
		int remainingAttempts = 2 - attemptNo;
		log.info("응답용 남은 시도 횟수 계산 완료: {}", remainingAttempts);

		// 뉴스 데이터 가져오기
		Long dailyContentId = sr.getDailyContentId().getValue();
		List<NewsItemSummary> newsItems = dailyContentQueryPort.loadNewsItemSummary(
			dailyContentId);
		log.info("뉴스 아이템 요약 조회 완료: {}", newsItems);

		return new SubmitQuizSubmissionResult(
			saved.getId(),
			saved.getDailyContentId().getValue(),
			new AttemptSummary(
				updatedAttempt.getId(),
				attemptNo,
				updatedAttempt.getAttemptStatus()
			),
			new SubmitQuizSubmissionResult.GradingSummary(
				correct,
				quizIds.size(),
				updatedAttempt.getAttemptResult()
			),
			newsItems,
			saved.getQuizProgressStatus(),
			saved.isNewsUnlocked(),
			remainingAttempts
		);
	}

}
