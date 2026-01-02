package com.oneco.backend.content.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneco.backend.content.domain.quiz.Quiz;

@Repository
public interface QuizJpaRepository extends JpaRepository<Quiz, Long> {

	// 사실상 @Query("SELECT q FROM Quiz q WHERE q.id IN :ids") 와 동일
	// ids가 [3,7 ,10] 이면 바인딩 값은 3,7,10 이 된다.
	// IN 절을 사용하여 여러 개의 id에 해당하는 Quiz 엔티티들을 한 번에 조회
	// 내가 가진 id 목록(3,7,10)에 해당하는 Quiz 엔티티들을 모두 가져온다.
	List<Quiz> findByIdIn(List<Long> ids);
}
