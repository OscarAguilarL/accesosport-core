package com.accesosport.invitation.application.usecase;

import com.accesosport.invitation.domain.exception.InvitationAlreadyUsedException;
import com.accesosport.invitation.domain.exception.InvitationNotFoundException;
import com.accesosport.invitation.domain.exception.InvitationRevokedException;
import com.accesosport.invitation.domain.model.InvitationStatus;
import com.accesosport.invitation.domain.model.InvitationToken;
import com.accesosport.invitation.domain.repository.InvitationTokenRepository;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class ValidateInvitationTokenUseCase {

    private final InvitationTokenRepository invitationTokenRepository;
    private final UserRepository userRepository;

    public Result execute(UUID token) {
        InvitationToken invitation = invitationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvitationNotFoundException(MessageKeys.Invitations.INVITATION_NOT_FOUND));

        if (invitation.getStatus() == InvitationStatus.USED) {
            throw new InvitationAlreadyUsedException(MessageKeys.Invitations.INVITATION_ALREADY_USED);
        }
        if (invitation.getStatus() == InvitationStatus.REVOKED) {
            throw new InvitationRevokedException(MessageKeys.Invitations.INVITATION_REVOKED);
        }

        boolean accountExists = userRepository.existsByEmail(invitation.getEmail());
        return new Result(invitation.getEmail(), accountExists);
    }

    public record Result(String email, boolean accountExists) {}
}
