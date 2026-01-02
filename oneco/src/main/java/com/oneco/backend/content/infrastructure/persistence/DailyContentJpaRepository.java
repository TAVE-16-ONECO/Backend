package com.oneco.backend.content.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oneco.backend.content.domain.dailycontent.DailyContent;

@Repository
public interface DailyContentJpaRepository extends JpaRepository<DailyContent, Long> {

	@Query("""
		select distinct dc
		from DailyContent dc
		left join fetch dc.quizzes q
		where dc.id = :id
		""")
	Optional<DailyContent> findByIdWithQuizzes(@Param("id") Long id);
}
