package com.oneco.backend.category.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneco.backend.category.domain.category.Category;

@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
}