package com.oneco.backend.mission.application.port.in;

import com.oneco.backend.mission.application.dto.CreateMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;

public interface CreateMissionUseCase {

	MissionResult request(CreateMissionCommand command);
}
