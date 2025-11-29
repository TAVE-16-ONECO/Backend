package com.oneco.backend.mission.application.port.out;

public record FamilyRelationInfo(

	Long id,
	Long parentId,
	Long childId,
	String status // ex) "PENDING", "USED", "EXPIRATION"

) {
	public boolean isUsed() {
		return "USED".equals(this.status);
	}

	public boolean isParent(Long memberId) {
		return this.parentId.equals(memberId);
	}

	public boolean isChild(Long memberId) {
		return this.childId.equals(memberId);
	}

}
