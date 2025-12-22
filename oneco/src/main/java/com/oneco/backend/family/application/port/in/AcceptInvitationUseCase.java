package com.oneco.backend.family.application.port.in;

import com.oneco.backend.family.application.dto.command.AcceptInvitationCommand;
import com.oneco.backend.family.application.dto.result.FamilyRelationResult;

public interface AcceptInvitationUseCase {
	FamilyRelationResult accept(AcceptInvitationCommand command);
}
