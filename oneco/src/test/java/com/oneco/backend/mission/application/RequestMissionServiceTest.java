package com.oneco.backend.mission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.mission.application.dto.RequestMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;
import com.oneco.backend.mission.application.port.out.MissionRepository;
import com.oneco.backend.mission.domain.Mission;
import com.oneco.backend.mission.domain.MissionStatus;
import com.oneco.backend.mission.domain.exception.MissionErrorCode;

@ExtendWith(MockitoExtension.class)
class RequestMissionServiceTest {

	@Mock
	private MissionRepository missionRepository;

	@InjectMocks
	private RequestMissionService requestMissionService;

	@Test
	@DisplayName("미션 생성 성공 시 기본 상태와 식별자를 반환하고 저장된 값이 일치한다")
	void createMission_success() throws Exception {

		// given
		RequestMissionCommand command = new RequestMissionCommand(
			1L,
			2L,
			LocalDate.now(),
			LocalDate.now().plusDays(3),
			"클로드 PRO 1년 결제",
			"개발 효율이 올라가요 ㅠㅠ"
		);

		when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> {
			Mission mission = invocation.getArgument(0);
			Field idField = Mission.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(mission, 10L); // persistence 모킹
			return mission;
		});

		// when
		MissionResult result = requestMissionService.request(command);

		// then
		assertThat(result.missionId()).isEqualTo(10L);
		assertThat(result.status()).isEqualTo(MissionStatus.APPROVAL_REQUEST);

		ArgumentCaptor<Mission> captor = ArgumentCaptor.forClass(Mission.class);
		verify(missionRepository).save(captor.capture());
		Mission saved = captor.getValue();
		assertThat(saved.getFamilyRelationId()).isEqualTo(2L);
		assertThat(saved.getPeriod().getStartDate()).isEqualTo(command.startDate());
		assertThat(saved.getPeriod().getEndDate()).isEqualTo(command.endDate());
	}

	@Test
	@DisplayName("보상이 비어 있으면 예외를 던진다")
	void createMission_blankReward_throws() {
		RequestMissionCommand command = new RequestMissionCommand(
			1L,
			2L,
			LocalDate.now(),
			LocalDate.now().plusDays(3),
			" ",
			"메시지"
		);

		assertThatThrownBy(() -> requestMissionService.request(command))
			.isInstanceOf(BaseException.class)
			.hasFieldOrPropertyWithValue("code", MissionErrorCode.MISSION_REWARD_CANNOT_BE_BLANK.getCode());
	}

	@Test
	@DisplayName("시작일이 종료일보다 늦으면 예외를 던진다")
	void createMission_invalidPeriod_throws() {
		RequestMissionCommand command = new RequestMissionCommand(
			1L,
			2L,
			LocalDate.now().plusDays(5), // 시작일
			LocalDate.now(), // 종료일
			"보상",
			"메시지"
		);

		assertThatThrownBy(() -> requestMissionService.request(command))
			.isInstanceOf(BaseException.class)
			.hasFieldOrPropertyWithValue("code", MissionErrorCode.INVALID_MISSION_TIME_ORDER.getCode());
	}
}
