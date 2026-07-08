package com.accesosport.invitation.application.usecase;

import com.accesosport.invitation.domain.exception.InvitationNotFoundException;
import com.accesosport.invitation.domain.model.InvitationToken;
import com.accesosport.invitation.domain.repository.InvitationTokenRepository;
import com.accesosport.shared.domain.i18n.MessageKeys;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class RevokeInvitationUseCase {

    private final InvitationTokenRepository invitationTokenRepository;

    public void execute(UUID token) {
        InvitationToken invitation = invitationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvitationNotFoundException(MessageKeys.Invitations.INVITATION_NOT_FOUND));

        invitation.revoke();
        invitationTokenRepository.save(invitation);
    }
}
