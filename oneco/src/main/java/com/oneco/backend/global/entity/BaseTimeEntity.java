package com.oneco.backend.global.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
/*
* JPA에서 이 클래스를 엔티티의 매핑 정보를 제공하는 상위 클래스로 취급함.
* 이 클래스 자체로는 테이블이 생성되지 않고, 필드의 매핑(`@Id`, `@Column`, 날짜/감사 필드 등)이 하위 엔티티에 상속되어 적용됨.
* 직접 쿼리 대상이 아니며, 공통 속성(예: id, createdAt, updatedAt)을 중복 없이 재사용할 때 사용.
 */
@EntityListeners(AuditingEntityListener.class)
/*
* 지정한 리스너 클래스를 엔티티의 라이프사이클 콜백(`@PrePersist`, `@PostLoad`, `@PreUpdate`, `@PreRemove` 등)을 처리하도록 등록함.
* 리스너는 별도 클래스에 콜백 어노테이션이 붙은 메서드를 두거나 엔티티 자체에 콜백 메서드를 둘 수 있음.
* Spring Data JPA의 감사 기능을 쓰려면 `AuditingEntityListener.class`를 등록하고, 설정에서 감사 기능을 활성화해야 함.
 */
public abstract class BaseTimeEntity {

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private Instant updatedAt;

}
