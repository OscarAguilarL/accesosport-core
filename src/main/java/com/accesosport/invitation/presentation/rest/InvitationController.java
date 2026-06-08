package com.accesosport.invitation.presentation.rest;

import com.accesosport.auth.infrastructure.security.CustomUserDetails;
import com.accesosport.invitation.application.dto.CreateInvitationRequest;
import com.accesosport.invitation.application.dto.InvitationResponse;
import com.accesosport.invitation.application.dto.InvitationValidationResponse;
import com.accesosport.invitation.application.service.InvitationApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationApplicationService invitationApplicationService;

    @PostMapping("/api/v1/admin/invitations")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<InvitationResponse> createInvitation(
            @Valid @RequestBody CreateInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        InvitationResponse response = invitationApplicationService.createInvitation(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/admin/invitations")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<InvitationResponse>> listInvitations() {
        return ResponseEntity.ok(invitationApplicationService.listInvitations());
    }

    @DeleteMapping("/api/v1/admin/invitations/{token}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> revokeInvitation(@PathVariable UUID token) {
        invitationApplicationService.revokeInvitation(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/public/invitations/{token}")
    public ResponseEntity<InvitationValidationResponse> validateToken(@PathVariable UUID token) {
        return ResponseEntity.ok(invitationApplicationService.validateToken(token));
    }
}
