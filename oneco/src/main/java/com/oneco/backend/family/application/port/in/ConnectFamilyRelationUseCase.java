package com.oneco.backend.family.application.port.in;

import com.oneco.backend.family.application.dto.command.ConnectFamilyRelationCommand;
import com.oneco.backend.family.application.dto.result.FamilyRelationResult;

public interface ConnectFamilyRelationUseCase {
	FamilyRelationResult connect(ConnectFamilyRelationCommand command);
}
