package com.accesosport.invitation.application.service;

import com.accesosport.invitation.application.dto.CreateInvitationRequest;
import com.accesosport.invitation.application.dto.InvitationResponse;
import com.accesosport.invitation.application.dto.InvitationValidationResponse;
import com.accesosport.invitation.application.usecase.CreateInvitationUseCase;
import com.accesosport.invitation.application.usecase.ListInvitationsUseCase;
import com.accesosport.invitation.application.usecase.RevokeInvitationUseCase;
import com.accesosport.invitation.application.usecase.ValidateInvitationTokenUseCase;
import com.accesosport.invitation.domain.repository.InvitationTokenRepository;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.domain.port.EmailTemplatePort;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationApplicationService {

    private final InvitationTokenRepository invitationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailTemplatePort emailTemplatePort;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional
    public InvitationResponse createInvitation(CreateInvitationRequest request, UUID adminId) {
        var command = new CreateInvitationUseCase.Command(request.email(), request.reason(), adminId);
        var token = new CreateInvitationUseCase(invitationTokenRepository, emailService, emailTemplatePort, frontendUrl)
                .execute(command);
        return InvitationResponse.fromDomain(token);
    }

    public InvitationValidationResponse validateToken(UUID token) {
        var result = new ValidateInvitationTokenUseCase(invitationTokenRepository, userRepository)
                .execute(token);
        return new InvitationValidationResponse(result.email(), result.accountExists());
    }

    public List<InvitationResponse> listInvitations() {
        return new ListInvitationsUseCase(invitationTokenRepository)
                .execute()
                .stream()
                .map(InvitationResponse::fromDomain)
                .toList();
    }

    @Transactional
    public void revokeInvitation(UUID token) {
        new RevokeInvitationUseCase(invitationTokenRepository).execute(token);
    }
}
