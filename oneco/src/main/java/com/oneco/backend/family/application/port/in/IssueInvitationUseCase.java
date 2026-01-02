package com.oneco.backend.family.application.port.in;

import com.oneco.backend.family.application.dto.command.IssueInvitationCommand;
import com.oneco.backend.family.application.dto.result.IssueInvitationResult;

public interface IssueInvitationUseCase {
	IssueInvitationResult issue(IssueInvitationCommand command);
}
