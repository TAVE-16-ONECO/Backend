package com.oneco.backend.StudyRecord.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.StudyRecord.application.dto.command.StartQuizAttemptCommand;
import com.oneco.backend.StudyRecord.application.dto.command.StartStudyCommand;
import com.oneco.backend.StudyRecord.application.dto.command.SubmitQuizSubmissionCommand;
import com.oneco.backend.StudyRecord.application.dto.result.StartQuizAttemptResult;
import com.oneco.backend.StudyRecord.application.dto.result.StartStudyResult;
import com.oneco.backend.StudyRecord.application.dto.result.SubmitQuizSubmissionResult;
import com.oneco.backend.StudyRecord.application.port.in.StartQuizAttemptUseCase;
import com.oneco.backend.StudyRecord.application.port.in.StartStudyUseCase;
import com.oneco.backend.StudyRecord.application.port.in.SubmitQuizSubmissionUseCase;
import com.oneco.backend.global.response.DataResponse;
import com.oneco.backend.global.security.jwt.JwtPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-records")
@Tag(name = "StudyRecord", description = "학습 기록 관련 API")
public class StudyRecordController {
	private final StartStudyUseCase startStudyUseCase;
	private final StartQuizAttemptUseCase startQuizAttemptUseCase;
	private final SubmitQuizSubmissionUseCase submitQuizSubmissionUseCase;

	// 1) 마스터하기 (학습 시작)

	/**
	 * 요청 예시:
	 * POST /api/study-records/start
	 * Headers:
	 *  Authorization: Bearer {JWT_TOKEN}
	 * Body:
	 * {
	 *  "dailyContentId": 123
	 *  }
	 */
	@Operation(
		summary = "학습 시작(마스터하기)",
		description = "dailyContentId로 학습(StudyRecord)을 시작합니다."
	)
	@PostMapping("/start")
	public ResponseEntity<DataResponse<StartStudyResult>> startStudy(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,

		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "학습 시작 요청",
			content = @Content(
				examples = @ExampleObject(
					name = "요청 예시",
					value = """
						{
						  "dailyContentId": 123
						}
						"""
				)
			)
		)
		@RequestBody @Valid StartStudyCommand command
	) {
		StartStudyResult result = startStudyUseCase.start(command, principal.memberId());

		return ResponseEntity.ok(DataResponse.from(result));
	}

	/**
	 * 퀴즈 도전하기 요청 예시:
	 * POST /api/study-records/{studyRecordId}/quiz-attempts
	 * Headers:
	 *  Authorization: Bearer {JWT_TOKEN}
	 */
	//2) 퀴즈 도전하기(시도 생성)
	@Operation(
		summary = "퀴즈 시도 생성(도전하기)",
		description = "studyRecordId에 대해 새로운 퀴즈 attempt를 생성합니다."
	)
	@PostMapping("/{studyRecordId}/quiz-attempts")
	public ResponseEntity<DataResponse<StartQuizAttemptResult>> startQuizAttempt(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,

		@Parameter(description = "학습 기록 ID", example = "10", required = true)
		@PathVariable @NotNull Long studyRecordId
	) {
		StartQuizAttemptCommand command = StartQuizAttemptCommand.with(studyRecordId);
		StartQuizAttemptResult result = startQuizAttemptUseCase.start(command, principal.memberId());

		return ResponseEntity.ok(DataResponse.from(result));
	}

	/**
	 * 퀴즈 제출하기 요청 예시:
	 * POST /api/study-records/{studyRecordId}/quiz-attempts/{attemptId}/submissions
	 * Headers:
	 *  Authorization: Bearer {JWT_TOKEN}
	 * Body:
	 * {
	 *  "answers": {
	 *       "1001": 1,
	 *       "1002": 0,
	 *       "1003": 2
	 *   }
	 * }
	 */
	// 3) 퀴즈 제출하기
	@Operation(
		summary = "퀴즈 제출",
		description = "attemptId에 대한 사용자의 답안을 제출하고 채점 결과를 반환합니다."
	)
	@PostMapping("/{studyRecordId}/quiz-attempts/{attemptId}/submissions")
	public ResponseEntity<DataResponse<SubmitQuizSubmissionResult>> submitQuiz(
		@Parameter(hidden = true)
		@AuthenticationPrincipal JwtPrincipal principal,

		@Parameter(description = "학습 기록 ID", example = "10", required = true)
		@PathVariable @NotNull Long studyRecordId,

		@Parameter(description = "퀴즈 시도 ID", example = "5", required = true)
		@PathVariable @NotNull Long attemptId,

		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "답안 제출 요청",
			content = @Content(
				examples = @ExampleObject(
					name = "요청 예시",
					value = """
						       {
						               "answers": {
						    "1001": 1,
						    "1002": 0,
						    "1003": 2
						}
						       }
						"""
				)
			)
		)
		@Parameter(description = "퀴즈 제출 요청", required = true)
		@RequestBody @Valid SubmitQuizSubmissionCommand command
	) {
		SubmitQuizSubmissionCommand commandWithPath = command.withPath(studyRecordId, attemptId);
		SubmitQuizSubmissionResult result = submitQuizSubmissionUseCase.submit(commandWithPath, principal.memberId());

		return ResponseEntity.ok(DataResponse.from(result));
	}

}
