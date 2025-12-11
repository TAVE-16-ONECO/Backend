package com.oneco.backend.content.domain.news;

import com.oneco.backend.content.domain.common.ImageFile;
import com.oneco.backend.content.domain.common.WebLink;
import com.oneco.backend.content.infrastructure.converter.NewsItemOrderConverter;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Table(name="news",
	uniqueConstraints = {
		@UniqueConstraint(
			// 같은 날에 나오는 뉴스의 순서는 중복될 수 없다.
			name = "uk_daily_item_order",
			columnNames = {"daily_content_id", "item_order"}
		)
	})
@Getter
public class NewsItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="title", nullable = false, length = 200)
	private String title;

	@Convert(converter = NewsItemOrderConverter.class)
	@Column(name="item_order", nullable = false)
	private NewsItemOrder newsItemOrder;

	// name="url"은 WebLink 내부 필드 이름
	// WebLink의 url 필드를 news_url 컬럼에 매핑
	@Embedded
	@AttributeOverride(name = "url", column = @Column(name="news_url", nullable = false))
	private WebLink webLink;

	@Embedded
	private ImageFile imageFile;

	private NewsItem(String title, WebLink webLink, NewsItemOrder newsItemOrder, ImageFile imageFile) {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("뉴스 title은 비어 있을 수 없습니다.");
		}
		if(webLink == null) {
			throw new IllegalArgumentException("뉴스 webLink는 null일 수 없습니다.");
		}
		if(newsItemOrder == null) {
			throw new IllegalArgumentException("뉴스 newsItemOrder는 null일 수 없습니다.");
		}
		if(imageFile == null) {
			throw new IllegalArgumentException("뉴스 imageFile는 null일 수 없습니다.");
		}
		this.title = title.trim();
		this.webLink = webLink;
		this.newsItemOrder = newsItemOrder;
		this.imageFile = imageFile;
	}
	public static NewsItem create(String title, WebLink webLink, NewsItemOrder newsItemOrder, ImageFile imageFile) {
		return new NewsItem(title, webLink, newsItemOrder, imageFile);
	}

	public void changeTitle(String newTitle) {
		if (newTitle == null || newTitle.isBlank()) {
			throw new IllegalArgumentException("뉴스 title은 비어 있을 수 없습니다.");
		}
		this.title = newTitle.trim();
	}

}
