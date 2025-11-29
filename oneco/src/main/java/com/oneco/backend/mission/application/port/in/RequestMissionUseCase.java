package com.oneco.backend.mission.application.port.in;

import com.oneco.backend.mission.application.dto.RequestMissionCommand;
import com.oneco.backend.mission.application.dto.MissionResult;

public interface RequestMissionUseCase {

	MissionResult request(RequestMissionCommand command);
}
