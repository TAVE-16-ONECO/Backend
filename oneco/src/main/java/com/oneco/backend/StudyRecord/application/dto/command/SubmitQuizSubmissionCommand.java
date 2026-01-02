package com.oneco.backend.StudyRecord.application.dto.command;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

/**
 * 제출하기 요청 DTO
 * JSON 예:
 * {
 * "answers": {
 * "1001": 1,
 * "1002": 0,
 * "1003": 2
 * }
 * }
 * - Jackson은 역직렬화할 때 대상 타입이 Map<Long, Integer>인 경우 키를 Long으로 변환하려고 시도
 * - 따라서 JSON의 키는 문자열이어야 하지만, Jackson이 이를 Long으로 변환할 수 있다.
 * - 값은 Integer로 그대로 매핑됨
 */
public record SubmitQuizSubmissionCommand(
	@NotNull
	@JsonProperty("answers")
	Map<Long, Integer> answers,

	// path로 들어오므로 처음엔 null, 컨트롤러에서 withPath 메서드로 채워줌
	Long studyRecordId,
	Long attemptId
) {
	public SubmitQuizSubmissionCommand withPath(Long studyRecordId, Long attemptId) {
		return new SubmitQuizSubmissionCommand(this.answers, studyRecordId, attemptId);
	}
}
