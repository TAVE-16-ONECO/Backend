package com.oneco.backend.mission.application.port.in;

import com.oneco.backend.mission.application.dto.ApproveMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;

public interface ApproveMissionUseCase {

	MissionResult decide(ApproveMissionCommand command);
}
