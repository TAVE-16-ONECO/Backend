package com.oneco.backend.Category.domain.Category;

import com.oneco.backend.Category.domain.exception.constant.CategoryErrorCode;
import com.oneco.backend.global.exception.BaseException;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 제목
	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "title", nullable = false, length = CategoryTitle.MAX_LENGTH))
	private CategoryTitle title;

	// 요약
	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "summary", nullable = false, length = CategorySummary.MAX_LENGTH))
	private CategorySummary summary;

	// 노출 순서
	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "display_order", nullable = true))
	private DisplayOrder displayOrder;

	// 기본 미션 일수
	@Embedded
	@AttributeOverride(name = "value", column = @Column(name = "default_mission_days", nullable = false))
	private MissionDays defaultMissionDays;

	// 카테고리 상태 (현재는 VISIBLE로 고정)
	@Enumerated(EnumType.STRING)
	@Column(name = "visibility", nullable = false, length = 20)
	private CategoryVisibility visibility = CategoryVisibility.VISIBLE;

	// 카테고리 난이도 (현재는 EASY로 고정)
	@Enumerated(EnumType.STRING)
	@Column(name = "difficulty", nullable = false, length = 20)
	private CategoryDifficulty difficulty = CategoryDifficulty.EASY;

	// 생성 메서드
	// 상태와 난이도는 기본값으로 설정(VISIBLE, EASY)
	// displayOrder는 현재 null
	// 따라서 create 입력값에서는 제외
	public static Category create(
		CategoryTitle title,
		CategorySummary summary,
		MissionDays defaultMissionDays
	) {
		if (title == null) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_REQUIRED_VALUE_MISSING, "title is null");
		}
		if (summary == null) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_REQUIRED_VALUE_MISSING, "summary is null");
		}
		// 현재 단계에서는 displayOrder는 null 허용
		// if (displayOrder == null) {
		// 	throw BaseException.from(CategoryErrorCode.CATEGORY_REQUIRED_VALUE_MISSING, "displayOrder is null");
		// }
		if (defaultMissionDays == null) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_REQUIRED_VALUE_MISSING, "defaultMissionDays is null");
		}

		Category category = new Category();
		category.title = title;
		category.summary = summary;
		category.defaultMissionDays = defaultMissionDays;
		return category;
	}

	public void renameCategoryTitle(CategoryTitle newTitle) {
		if (newTitle == null) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_REQUIRED_VALUE_MISSING, "newTitle is null");
		}
		this.title = newTitle;
	}

	public void updateSummary(CategorySummary newSummary) {
		if (newSummary == null) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_REQUIRED_VALUE_MISSING, "newSummary is null");
		}
		this.summary = newSummary;
	}

	public void changeDisplayOrder(DisplayOrder newDisplayOrder) {
		if (newDisplayOrder == null) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_REQUIRED_VALUE_MISSING, "newDisplayOrder is null");
		}
		this.displayOrder = newDisplayOrder;
	}

	public void changeDefaultMissionDays(MissionDays newMissionDays) {
		if (newMissionDays == null) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_REQUIRED_VALUE_MISSING, "newMissionDays is null");
		}
		this.defaultMissionDays = newMissionDays;
	}

	public void hideCategory() {
		if (this.visibility == CategoryVisibility.HIDDEN) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_ALREADY_HIDDEN);
		}
		this.visibility = CategoryVisibility.HIDDEN;
	}

	public void showCategory() {
		if (this.visibility == CategoryVisibility.VISIBLE) {
			throw BaseException.from(CategoryErrorCode.CATEGORY_ALREADY_VISIBLE);
		}
		this.visibility = CategoryVisibility.VISIBLE;
	}

}
