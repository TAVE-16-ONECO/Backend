package com.oneco.backend.global.response;

import java.util.List;

import lombok.Getter;

@Getter
public class CursorResponse<T> {

	private final List<T> content;  // 실제 데이터 리스트
	private final Long nextCursor;  // 다음 요청 시 보낼 커서 (마지막 ID)
	private final boolean hasNext;  // 다음 페이지 존재 여부

	// 생성자 (private으로 막고 factory method 사용 권장)
	private CursorResponse(List<T> content, Long nextCursor, boolean hasNext) {
		this.content = content;
		this.nextCursor = nextCursor;
		this.hasNext = hasNext;
	}

	// 정적 팩토리 메서드
	public static <T> CursorResponse<T> of(List<T> content, Long nextCursor, boolean hasNext) {
		return new CursorResponse<>(content, nextCursor, hasNext);
	}
}
