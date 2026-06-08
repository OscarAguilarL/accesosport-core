package com.accesosport.invitation.application.usecase;

import com.accesosport.invitation.domain.model.InvitationToken;
import com.accesosport.invitation.domain.repository.InvitationTokenRepository;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ListInvitationsUseCase {

    private final InvitationTokenRepository invitationTokenRepository;

    public List<InvitationToken> execute() {
        return invitationTokenRepository.findAll();
    }
}
