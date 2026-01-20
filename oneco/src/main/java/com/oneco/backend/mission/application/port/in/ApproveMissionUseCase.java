package com.oneco.backend.mission.application.port.in;

import com.oneco.backend.mission.application.dto.ApproveMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;

public interface ApproveMissionUseCase {

	// 미션 수신자는 미션 승인 또는 거절을 결정한다.
	MissionResult decide(ApproveMissionCommand command);
}
