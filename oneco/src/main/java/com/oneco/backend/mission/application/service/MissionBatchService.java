package com.oneco.backend.mission.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oneco.backend.mission.application.port.out.MissionPersistencePort;
import com.oneco.backend.mission.domain.mission.Mission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 배치/스케쥴러
// 언제, 누구를 실패 처리할 지 결정한다.
@Slf4j
@Component
@RequiredArgsConstructor
public class MissionBatchService {
	// 1. MissionPersistencePort를 이용하여 마감기한이 지난 미션들을 DB 에서 조회한다.
	// 2. 조회된 미션들을 순회하며 MissionStatusChanger.toFailed()를 호출한다.

	private final MissionPersistencePort missionPort;
	private final MissionStatusChanger missionStatusChanger;

	// 매일 자정에 실행한다.
	@Scheduled(cron = "0 0 0 * * *") // 매일 자정
	public void failOverdueMissions() {
		LocalDate now = LocalDate.now(); // 현재 날짜

		// 1. 마감기한이 지났고, 아직 완료되지 않은 (진행중인) 미션들을 조회한다.
		List<Mission> overdueMissions = missionPort.findAllOverdueMissions(now);

		if (overdueMissions.isEmpty()) {
			// 처리할 미션이 없다면 종료
			log.info("[MissionBatchService] 마감기한이 지난 미션이 없습니다. - {}", LocalDateTime.now());
			return;
		}
		log.info("[MissionBatchService] 마감기한이 지난 미션 {}건 처리 시작 - {}", overdueMissions.size(), LocalDateTime.now());

		// 2. 순회하며 외부 서비스 호출한다. (프록시 타게 됨 -> 트랜잭션 정상 작동)
		int successCount = 0;
		for (Mission mission : overdueMissions) {
			try {
				// 여기서 외부 빈(Bean)의 호출 -> 프록시를 타게 됨 -> 트랜잭션 정상 작동
				missionStatusChanger.processBatchFailure(mission);
				successCount++;
			} catch (Exception e) {
				log.error("[MissionBatchService] 미션 ID {} 실패 처리 중 오류 발생: {}", mission.getId(), e.getMessage());
			}
		}
		log.info("[MissionBatchService] 처리 완료. 성공: {}/{}", successCount, overdueMissions.size());
	}

}

