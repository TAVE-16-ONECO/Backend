package com.oneco.backend.category.infrastructure;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneco.backend.category.domain.category.Category;
import com.oneco.backend.category.domain.category.CategoryVisibility;

@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
	List<Category> findByVisibility(CategoryVisibility visibility, Sort sort);
}
