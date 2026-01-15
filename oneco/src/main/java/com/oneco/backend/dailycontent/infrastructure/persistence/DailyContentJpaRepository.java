package com.oneco.backend.dailycontent.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oneco.backend.dailycontent.domain.dailycontent.DailyContent;
import com.oneco.backend.dailycontent.domain.dailycontent.DaySequence;

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

	// 여러 DailyContent를 NewsItems와 함께 조회한다.
	// distinct를 사용하는 이유:
	// - DailyContent와 NewsItems는 일대다 관계이므로
	// - 조인 시 동일한 DailyContent가 여러 번 나타날 수 있음
	// - 이를 방지하기 위해 distinct를 사용하여 중복을 제거함
	@Query("""
		select distinct dc
		from DailyContent dc
		left join fetch dc.newsItems ni
		where dc.id in :dailyContentIds
		"""
	)
	List<DailyContent> findAllWithNewsItemsByIdIn(@Param("dailyContentIds") List<Long> dailyContentIds);
}
