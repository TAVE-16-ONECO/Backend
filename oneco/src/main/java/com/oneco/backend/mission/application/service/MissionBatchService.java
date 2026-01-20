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
		long successCount = 0;
		long errorCount = 0; // 실패 처리 중 오류 발생 건수

		// 1. 마감기한이 지났고, 아직 완료되지 않은 (진행중인) 미션들을 조회한다.
		List<Mission> overdueMissions = missionPort.findAllOverdueMissions(now);

		// =============================
		// 스케줄러 로그 출력
		// =============================

		if (overdueMissions.isEmpty()) {
			// 1. 처리할 미션이 없다면 종료
			log.info("[MissionBatchService-failOverdueMissions] 마감기한이 지난 미션이 없습니다. - {}", LocalDateTime.now());
			return;
		}

		// 2. 처리할 미션이 있다면 로그 출력
		log.info(
			"[MissionBatchService-failOverdueMissions] 마감기한이 지난 미션 {}건 처리 시작 - {}",
			overdueMissions.size(),
			LocalDateTime.now()
		);

		// =============================
		// 미션 실패 처리
		// =============================

		// 1. 순회하며 외부 서비스 호출한다. (프록시 타게 됨 -> 트랜잭션 정상 작동)
		for (Mission mission : overdueMissions) {
			try {
				// 여기서 외부 빈(Bean)의 호출 -> 프록시를 타게 됨 -> 트랜잭션 정상 작동
				missionStatusChanger.processBatchFailure(mission);
				successCount++;
			} catch (Exception e) {
				errorCount++;
				log.error("[MissionBatchService-failOverdueMissions] 미션 ID {} 실패 처리 중 오류", mission.getId(), e);
			}
		}

		log.info("[MissionBatchService-failOverdueMissions] 처리 완료. 성공: {}/{}", successCount, overdueMissions.size());
		log.info("[MissionBatchService-failOverdueMissions] 오류 발생 건수: {}", errorCount);
	}

	// 매일 00:10 에 실행한다.
	// 미션 수락 상태에서 Mission의 startDate이 되면 미션 진행중으로 변경한다.
	@Scheduled(cron = "0 10 0 * * *")
	public void startScheduledMissions() {
		LocalDate today = LocalDate.now(); // 현재 날짜
		long successCount = 0; // 성공 처리 건수
		long errorCount = 0; // 실패 처리 중 오류 발생 건수

		// 1. 오늘이 시작일이면서 미션 상태가 APPROVAL_ACCEPTED인 미션들을 조회한다.
		List<Mission> missionsToStart = missionPort.findAllMissionsStartingToday(today);

		// =============================
		// 스케줄러 로그 출력
		// =============================

		// 1. 처리할 미션이 없다면 종료
		if (missionsToStart.isEmpty()) {
			log.info(
				"[MissionBatchService-startScheduledMissions] 오늘 시작할 미션이 없습니다. - {}", LocalDateTime.now()
			);
			return;
		}

		// 2. 처리할 미션이 있다면 로그 출력
		log.info(
			"[MissionBatchService-startScheduledMissions] 오늘 시작하는 미션 {}건 처리 시작 - {}",
			missionsToStart.size(),
			LocalDateTime.now()
		);

		// =============================
		// 미션 진행중 상태로 변경 처리
		// =============================

		// 1. 순회하며 미션을 진행중 상태로 변경한다.
		for (Mission mission : missionsToStart) {
			try {
				// 여기서 외부 빈(Bean)의 호출 -> 프록시를 타게 됨 -> 트랜잭션 정상 작동
				missionStatusChanger.toInProgress(mission);
				successCount++;
				log.info("[MissionBatchService-startScheduledMissions] 미션 ID {} 진행중 상태로 변경 완료", mission.getId());
			} catch (Exception e) {
				errorCount++;
				log.error("[MissionBatchService-startScheduledMissions] 미션 ID {} 진행중 전환 실패", mission.getId(), e);
			}
		}

		log.info("[MissionBatchService-startScheduledMissions] 오늘 시작하는 미션 처리 완료. 성공: {}/{}", successCount,
			missionsToStart.size());
		log.info("[MissionBatchService-startScheduledMissions] 오류 발생 건수: {}", errorCount);
	}

}
