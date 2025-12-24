package com.oneco.backend.StudyRecord.domain.studyRecord;

/**
 * 학습 진행 상태(서버가 강제하는 상태 머신)
 * <p>
 * KEYWORD_SHOWN: 홈에서 오늘 키워드 노출(기록 생성 시점에 따라)
 * STUDY_OPENED: "학습하기" 눌러 본문 열람
 * QUIZ_IN_PROGRESS: 퀴즈 시도 생성 후 제출 대기
 * PASSED: 정답 3개 모두 맞춤
 * FAILED: 2회차까지 실패(기회 소진)
 */
public enum StudyStatus {
	KEYWORD_SHOWN,
	STUDY_OPENED,
	QUIZ_IN_PROGRESS,
	PASSED,
	FAILED
}