package com.accesosport.invitation.application.usecase;

import com.accesosport.invitation.domain.model.InvitationToken;
import com.accesosport.invitation.domain.repository.InvitationTokenRepository;
import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.domain.port.EmailTemplatePort;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CreateInvitationUseCase {

    private final InvitationTokenRepository invitationTokenRepository;
    private final EmailService emailService;
    private final EmailTemplatePort emailTemplatePort;
    private final String frontendUrl;

    public InvitationToken execute(Command command) {
        InvitationToken invitation = InvitationToken.create(command.email(), command.reason(), command.adminId());
        InvitationToken saved = invitationTokenRepository.save(invitation);

        String link = frontendUrl + "/organizadores/registro?token=" + saved.getToken();
        emailService.send(EmailMessage.of(
                saved.getEmail(),
                "Bienvenido a AccesoSport — Crea tu cuenta como organizador",
                emailTemplatePort.buildOrganizerInvitationEmail(command.reason(), link)
        ));

        return saved;
    }

    public record Command(String email, String reason, UUID adminId) {}
}
