package com.oneco.backend.StudyRecord.presentation;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.StudyRecord.application.dto.command.HistoryViewMode;
import com.oneco.backend.StudyRecord.application.dto.result.HistoryResult;
import com.oneco.backend.StudyRecord.application.service.HistoryService;
import com.oneco.backend.global.response.DataResponse;
import com.oneco.backend.global.security.jwt.JwtPrincipal;
import com.oneco.backend.member.domain.FamilyRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-records/history")
public class StudyRecordHistoryController {

	private final HistoryService historyService;

	/**
	 * [공부 기록 히스토리 조회]
	 * <p>
	 * 이 API는 "커서 기반 페이지네이션" 방식이다.
	 * - 첫 페이지: 커서를 보내지 않는다. (lastStudyRecordId=null, lastSubmittedDate=null)
	 * - 다음 페이지: 이전 응답에서 받은 nextId / nextSubmittedDate를 그대로 보내서 이어서 조회한다.
	 * <p>
	 * 역할에 따른 동작
	 * - CHILD(자식 계정):
	 * - 본인의 히스토리를 조회한다.
	 * - childId는 null로 보낸다.
	 * <p>
	 * - PARENT(부모 계정):
	 * - 자녀 히스토리를 조회한다.
	 * - childId를 주면: 해당 자녀 히스토리를 조회(부모-자녀 관계 검증 필요)
	 * - childId가 null이면: "첫 번째 자녀"를 기본 선택해서 조회(서비스 정책)
	 * - 연결된 자녀가 0명이고 childId도 null이면: 빈 응답(memberItems=[], historyItems=[])이 내려감
	 * <p>
	 * null이 들어올 수 있는 상황
	 * - lastStudyRecordId == null:
	 * - 첫 페이지 조회일 때
	 * - lastSubmittedDate == null:
	 * - 첫 페이지 조회일 때
	 * - childId == null:
	 * - 부모가 특정 자녀를 선택하지 않았을 때 (첫 번째 자녀를 기본 선택)
	 * - 자식 계정은 원래 childId를 보내지 않으므로 null
	 */
	@Operation(
		summary = "공부 기록 히스토리 조회(커서 페이징)",
		description = """
			커서 기반 페이지네이션 방식으로 공부 기록 히스토리를 조회한다.
			최신순 -> viewMode = ALL , 북마크 기준 -> viewMode = BOOKMARKED
			모드를 변경하는 경우 커서를 초기화해서 서버로 보낸다.
			-> lastStudyRecordId, lastSubmittedDate를 보내지 않는다.
			
			[첫 페이지]
			- lastStudyRecordId, lastSubmittedDate를 보내지 않는다.
			- 예) /api/study-records/history?size=20&viewMode=ALL
			
			[다음 페이지]
			- 이전 응답의 nextId, nextSubmittedDate를 그대로 전달한다.
			- 예) /api/study-records/history?size=20&viewMode=ALL&lastStudyRecordId=123&lastSubmittedDate=2026-01-10
			
			[부모 계정]
			- childId를 주면 해당 자녀 히스토리를 조회한다.
			- childId가 없으면(=null) 첫 번째 자녀를 기본 선택한다
			"""
	)
	@GetMapping
	public ResponseEntity<DataResponse<HistoryResult>> getStudyRecordHistory(
		@Parameter(
			description = "커서: 마지막 학습 기록 ID (첫 페이지면 null)",
			required = false,
			schema = @Schema(example = "123")
		)
		@RequestParam(required = false) Long lastStudyRecordId,

		@Parameter(
			description = "커서: 마지막 제출일자 (첫 페이지면 null). yyyy-MM-dd 형식",
			required = false,
			schema = @Schema(example = "2026-01-10")
		)
		@RequestParam(required = false) LocalDate lastSubmittedDate,

		@Parameter(
			description = """
				부모가 자녀 기록 조회 시 사용할 자녀 ID.
				- 부모인데 childId가 null이면: 첫 번째 자녀를 기본 선택(서비스 정책)
				- 자식 계정이면: 의미 없으므로 보통 null
				""",
			required = false,
			schema = @Schema(example = "55")
		)
		@RequestParam(required = false) Long childId,

		@Parameter(
			description = "가져올 항목 수 (페이지 크기)",
			required = true,
			schema = @Schema(example = "20", minimum = "1", maximum = "100")
		)
		@RequestParam int size,

		@Parameter(
			description = "북마크 토글 여부",
			required = false,
			schema = @Schema(example = "ALL or BOOKMARKED")
		)
		@RequestParam(required = false, defaultValue = "ALL") HistoryViewMode viewMode,

		@AuthenticationPrincipal JwtPrincipal principal
	) {
		// 1) 인증 주체에서 memberId/role 추출
		// - memberId: CHILD면 본인, PARENT면 부모 id
		Long memberId = principal.memberId();

		String role = principal.familyRole();
		FamilyRole familyRole = FamilyRole.valueOf(role);

		// 2) 서비스 호출 후 결과 반환
		HistoryResult result = historyService.loadByRole(
			familyRole,
			memberId,
			childId,
			lastStudyRecordId,
			lastSubmittedDate,
			size,
			viewMode
		);

		return ResponseEntity.ok(DataResponse.from(result));
	}

}
