package com.oneco.backend.content.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oneco.backend.content.domain.dailycontent.DailyContent;
import com.oneco.backend.content.domain.dailycontent.DaySequence;

@Repository
public interface DailyContentJpaRepository extends JpaRepository<DailyContent, Long> {

	@Query("""
		select distinct dc
		from DailyContent dc
		left join fetch dc.quizzes q
		where dc.id = :id
		""")
	Optional<DailyContent> findByIdWithQuizzes(@Param("id") Long id);

	@Query("""
			select distinct dc
			from DailyContent  dc
			left join fetch dc.newsItems n
			where dc.id = :id
		""")
	Optional<DailyContent> findByIdWithNews(@Param("id") Long id);

	// elapsedDays는 1부터 시작한다고 가정한다.
	// 예: elapsedDays = 1 -> 첫째날, elapsedDays = 2 -> 둘째날
	// daySequence도 1부터 시작한다.
	// 예: daySequence = 1 -> 첫째날, daySequence = 2 -> 둘째날
	// 따라서 elapsedDays와 daySequence는 동일한 값을 가진다.
	// elapsedDays와 daySequence가 동일한 값을 가지므로, 이를 이용하여 조회한다.
	@Query("""
		select dc
		from DailyContent dc
		where dc.categoryId.value = :categoryId
		and dc.daySequence = :daySequence
		""")
	Optional<DailyContent> findByCategoryIdAndDaySequence(
		@Param("categoryId") Long categoryId,
		@Param("daySequence") DaySequence daySequence);

	// 카테고리의 모든 DailyContent를 순서대로 조회한다.
	List<DailyContent> findAllByCategoryId_ValueOrderByDaySequence(Long categoryId);
}
