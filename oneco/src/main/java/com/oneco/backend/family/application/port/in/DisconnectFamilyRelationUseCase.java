package com.oneco.backend.family.application.port.in;

import com.oneco.backend.family.application.dto.command.DisconnectFamilyRelationCommand;
import com.oneco.backend.family.application.dto.result.FamilyRelationResult;

public interface DisconnectFamilyRelationUseCase {
	FamilyRelationResult disconnect(DisconnectFamilyRelationCommand command);
}
