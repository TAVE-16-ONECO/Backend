package com.oneco.backend.StudyRecord.application.port.out;

import java.util.List;

import com.oneco.backend.StudyRecord.application.dto.result.MemberItem;

public interface FamilyRelationQueryPort {

	// 부모ID로 연결된 자녀ID들 조회
	List<MemberItem> findChildIdsByParentId(Long parentId);
}
