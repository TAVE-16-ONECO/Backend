package com.oneco.backend.mission.application.port.out;

public interface MemberReader {
	MemberInfo getById(Long memberId);
}
