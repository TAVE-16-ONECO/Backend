package com.oneco.backend.StudyRecord.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneco.backend.StudyRecord.application.dto.command.HistoryViewMode;
import com.oneco.backend.StudyRecord.application.dto.result.HistoryItem;
import com.oneco.backend.StudyRecord.application.dto.result.HistoryResult;
import com.oneco.backend.StudyRecord.application.dto.result.MemberItem;
import com.oneco.backend.StudyRecord.application.port.dto.DailyContentSummary;
import com.oneco.backend.StudyRecord.application.port.out.DailyContentQueryPort;
import com.oneco.backend.StudyRecord.application.port.out.FamilyRelationQueryPort;
import com.oneco.backend.StudyRecord.application.port.out.StudyRecordPersistencePort;
import com.oneco.backend.StudyRecord.domain.exception.constant.StudyErrorCode;
import com.oneco.backend.StudyRecord.domain.studyRecord.StudyRecord;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.member.domain.FamilyRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class HistoryService {

	private final StudyRecordPersistencePort studyRecordPersistencePort;
	private final DailyContentQueryPort dailyContentQueryPort;
	private final FamilyRelationQueryPort familyRelationQueryPort;

	/**
	 * [공통 입구]
	 * - 컨트롤러에서 role(부모/자식)을 받아 이 메서드로 진입한다.
	 * - 여기서 잠깐 분기(role에 따라 조회 대상 memberId가 달라짐)한 뒤,
	 * 최종적으로는 공통 조립 메서드(buildHistoryResult)를 통해 같은 형태의 응답을 만든다.
	 */
	@Transactional(readOnly = true)
	public HistoryResult loadByRole(
		FamilyRole role,
		Long memberId,
		Long childId,
		Long lastStudyRecordId,
		LocalDate lastSubmittedDate,
		int size,
		HistoryViewMode viewMode
	) {
		log.info(
			"[히스토리 조회] 요청 시작 - 역할={}, memberId={}, childId={}, lastStudyRecordId={}, lastSubmittedDate={}, size={}",
			role, memberId, childId, lastStudyRecordId, lastSubmittedDate, size);

		if (lastSubmittedDate != null && lastStudyRecordId == null) {
			throw BaseException.from(StudyErrorCode.INVALID_CURSOR);
		}
		if (role == FamilyRole.CHILD) {
			log.info("[히스토리 조회] 자식(본인) 조회로 분기");
			return getHistoryChild(memberId, lastStudyRecordId, lastSubmittedDate, size, viewMode);

		} else if (role == FamilyRole.PARENT) {
			log.info("[히스토리 조회] 부모(자녀 조회)로 분기");
			return getHistoryParent(memberId, childId, lastStudyRecordId, lastSubmittedDate, size, HistoryViewMode.ALL);

		} else {
			log.info("[히스토리 조회] 잘못된 역할 값 - role={}", role);
			throw BaseException.from(StudyErrorCode.INVALID_FAMILY_ROLE);
		}
	}

	/**
	 * [자식(본인) 히스토리 조회]
	 */
	public HistoryResult getHistoryChild(
		Long memberId,
		Long lastStudyRecordId,
		LocalDate lastSubmittedDate,
		int size,
		HistoryViewMode viewMode
	) {
		log.info("[히스토리 조회][자식] 본인 히스토리 조회 - memberId={}, lastStudyRecordId={}, lastSubmittedDate={}, size={}",
			memberId, lastStudyRecordId, lastSubmittedDate, size);

		return buildHistoryResult(memberId, null, lastStudyRecordId, lastSubmittedDate, size, viewMode);
	}

	/**
	 * [부모 히스토리 조회]
	 * - 부모는 자녀 목록을 내려줘야 함
	 * - childId가 없으면 첫 번째 자녀를 기본 선택
	 * - childId가 있으면 부모-자녀 관계 검증
	 */
	public HistoryResult getHistoryParent(
		Long parentId,
		Long childId,
		Long lastStudyRecordId,
		LocalDate lastSubmittedDate,
		int size,
		HistoryViewMode viewMode
	) {
		log.info("[히스토리 조회][부모] 부모 요청 - parentId={}, childId={}, lastStudyRecordId={}, lastSubmittedDate={}, size={}",
			parentId, childId, lastStudyRecordId, lastSubmittedDate, size);

		// 1) 부모에게 연결된 자녀 목록 조회
		List<MemberItem> memberItems = familyRelationQueryPort.findChildIdsByParentId(parentId);
		log.info("[히스토리 조회][부모] 연결된 자녀 목록 조회 완료 - 자녀 수={}", memberItems.size());

		// 2) childId가 없는 경우: 자식 없으면 빈 응답 / 있으면 첫 번째 자녀 선택
		if (childId == null) {
			if (memberItems.isEmpty()) {
				log.info("[히스토리 조회][부모] 연결된 자녀가 없음 - 빈 결과 반환");
				return new HistoryResult(
					false,
					null,
					null,
					List.of(),
					List.of()
				);
			}

			childId = memberItems.get(0).memberId();
			log.info("[히스토리 조회][부모] childId 미전달 -> 첫 번째 자녀로 기본 선택 - 선택된 childId={}", childId);

		} else {
			// 3) childId가 있는 경우: 부모-자녀 관계 검증
			boolean isChildOfParent = false;

			for (MemberItem item : memberItems) {
				// childId는 null이 아님(else 진입 조건)
				// item.memberId()가 null이어도 equals는 false 리턴(NullPointerException 없음)
				if (childId.equals(item.memberId())) {
					isChildOfParent = true;
					break;
				}
			}

			log.info("[히스토리 조회][부모] 부모-자녀 관계 검증 결과 - childId={}, isChildOfParent={}", childId, isChildOfParent);

			if (!isChildOfParent) {
				log.info("[히스토리 조회][부모] 요청한 childId가 부모의 자녀가 아님 - parentId={}, childId={}", parentId, childId);
				throw BaseException.from(StudyErrorCode.INVALID_CHILD_ACCESS);
			}
		}

		// 4) 최종 조회는 childId 대상으로 수행 + 자녀 목록(memberItems)도 함께 내려줌
		log.info("[히스토리 조회][부모] 자녀 히스토리 조회 시작 - 조회대상 childId={}, 자녀목록수={}", childId, memberItems.size());
		return buildHistoryResult(childId, memberItems, lastStudyRecordId, lastSubmittedDate, size, viewMode);
	}

	/**
	 * lastStudyRecordId, lastSubmittedDate 기준 커서 페이징 조회
	 * - 만약 모드가 바뀌면 클라이언트에서 커서 초기화해서 보내주면
	 * - 이 메서드에서는 그냥 받은 커서 기준으로 조회
	 * [공통 조립 메서드]
	 * - StudyRecord Slice 조회
	 * - DailyContentSummary 일괄 조회(Map)
	 * - HistoryItem 조립
	 * - nextCursor 계산 후 HistoryResult 반환
	 */
	private HistoryResult buildHistoryResult(
		Long memberId,
		List<MemberItem> memberItems,
		Long lastStudyRecordId,
		LocalDate lastSubmittedDate,
		int size,
		HistoryViewMode viewMode
	) {
		// memberItems가 null이면 빈 리스트로 통일
		if (memberItems == null) {
			memberItems = List.of();
		}

		log.info("[히스토리 공통] 공통 조립 시작 - 조회대상 memberId={}, lastStudyRecordId={}, lastSubmittedDate={}, size={}, 자녀목록수={}",
			memberId, lastStudyRecordId, lastSubmittedDate, size, memberItems.size());

		HistoryViewMode mode = (viewMode == null) ? HistoryViewMode.ALL : viewMode;
		// 1) StudyRecord 커서 페이징 조회
		// 모드에 따라 다른 쿼리 사용
		Slice<StudyRecord> slice = null;
		if (mode == HistoryViewMode.BOOKMARKED) {
			slice = studyRecordPersistencePort.findBookmarkedByLastStudyRecordIdAndMemberId(
				memberId,
				lastStudyRecordId,
				lastSubmittedDate,
				size
			);
		} else if (mode == HistoryViewMode.ALL) {
			slice = studyRecordPersistencePort.findByLastStudyRecordIdAndMemberId(
				memberId,
				lastStudyRecordId,
				lastSubmittedDate,
				size
			);
		}

		List<StudyRecord> records = slice.getContent();
		log.info("[히스토리 공통] StudyRecord 조회 완료 - recordsSize={}, hasNext={}", records.size(), slice.hasNext());

		// 2) records가 비면: historyItems는 빈 리스트, memberItems는 유지(부모 화면에서 필요할 수 있음)
		if (records.isEmpty()) {
			log.info("[히스토리 공통] 조회된 StudyRecord가 없음 - 빈 결과 반환(자녀목록은 유지)");
			return new HistoryResult(
				false,
				null,
				null,
				memberItems,  // 부모면 자녀목록 유지
				List.of()
			);
		}

		// 3) dailyContentId 추출 + 중복 제거
		Set<Long> idSet = new LinkedHashSet<>();
		for (int i = 0; i < records.size(); i++) {
			idSet.add(records.get(i).getDailyContentId().getValue());
		}
		List<Long> dailyContentIds = new ArrayList<>(idSet);
		log.info("[히스토리 공통] dailyContentId 추출 완료 - 중복 제거 후 개수={}", dailyContentIds.size());

		// 4) DailyContentSummary 일괄 조회(Map)
		Map<Long, DailyContentSummary> dailyContentSummaries =
			dailyContentQueryPort.findDailyContentSummariesByIds(dailyContentIds);
		log.info("[히스토리 공통] DailyContentSummary 조회 완료 - mapSize={}", dailyContentSummaries.size());

		// 5) HistoryItem 조립
		List<HistoryItem> historyItems = new ArrayList<>();
		for (StudyRecord sr : records) {
			Long dailyContentId = sr.getDailyContentId().getValue();
			DailyContentSummary dcs = dailyContentSummaries.get(dailyContentId);

			if (dcs == null) {
				log.info("[히스토리 공통][오류] DailyContentSummary 누락 - dailyContentId={}, studyRecordId={}",
					dailyContentId, sr.getId());
				throw BaseException.from(StudyErrorCode.DAILY_CONTENT_NOT_FOUND);
			}

			HistoryItem historyItem = new HistoryItem(
				sr.getId(),
				sr.getQuizSubmittedDate(),
				sr.isBookmarked(),
				dcs
			);
			historyItems.add(historyItem);
		}
		log.info("[히스토리 공통] HistoryItem 조립 완료 - historyItemsSize={}", historyItems.size());

		// 6) nextCursor 계산
		LocalDate nextSubmittedDate = null;
		Long nextId = null;

		boolean hasNext = slice.hasNext();
		if (hasNext) {
			StudyRecord last = records.get(records.size() - 1);
			nextId = last.getId();
			nextSubmittedDate = last.getQuizSubmittedDate();
		}
		log.info("[히스토리 공통] 다음 커서 계산 완료 - hasNext={}, nextId={}, nextSubmittedDate={}",
			hasNext, nextId, nextSubmittedDate);

		// 7) 최종 응답 반환
		log.info("[히스토리 공통] 최종 응답 반환 - hasNext={}, 자녀목록수={}, 히스토리개수={}",
			hasNext, memberItems.size(), historyItems.size());

		return new HistoryResult(
			hasNext,
			nextId,
			nextSubmittedDate,
			memberItems,
			historyItems
		);
	}
}