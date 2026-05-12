package com.accesosport.registration.presentation.rest;

import com.accesosport.auth.infrastructure.security.CustomUserDetails;
import com.accesosport.registration.application.dto.ParticipantInEventResponse;
import com.accesosport.registration.application.dto.RegisterParticipantRequest;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.application.service.RegistrationApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationApplicationService registrationApplicationService;

    /**
     * Registers the authenticated participant in the specified event.
     *
     * @param eventId     the unique identifier of the event
     * @param userDetails the authenticated participant
     * @return 201 Created with the new registration details
     */
    @PostMapping("/api/v1/events/{eventId}/register")
    @PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
    public ResponseEntity<RegistrationResponse> registerParticipant(
            @PathVariable UUID eventId,
            @RequestBody(required = false) RegisterParticipantRequest body,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID participantId = userDetails.getUserId();
        UUID modalityId = body != null ? body.modalityId() : null;
        boolean waiverAccepted = body != null && body.waiverAccepted();
        RegistrationResponse response = registrationApplicationService.registerParticipant(eventId, participantId, modalityId, waiverAccepted);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cancels a registration.
     * A participant may cancel their own registration; an organizer or admin may cancel any registration.
     *
     * @param eventId        the event the registration belongs to (path binding only)
     * @param registrationId the unique identifier of the registration to cancel
     * @param userDetails    the authenticated user
     * @return 200 OK with the cancelled registration details
     */
    @DeleteMapping("/api/v1/events/{eventId}/registrations/{registrationId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PARTICIPANT', 'ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<RegistrationResponse> cancelRegistration(
            @PathVariable UUID eventId,
            @PathVariable UUID registrationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isAdmin = isAdminOrOrganizer(userDetails);
        UUID requesterId = isAdmin ? null : userDetails.getUserId();
        RegistrationResponse response = registrationApplicationService.cancelRegistration(registrationId, requesterId, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the list of all registrations for a given event.
     * Only organizers and admins can access this endpoint.
     *
     * @param eventId the unique identifier of the event
     * @return 200 OK with the list of participant registrations
     */
    @GetMapping("/api/v1/events/{eventId}/registrations")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<List<ParticipantInEventResponse>> getEventRegistrations(
            @PathVariable UUID eventId
    ) {
        List<ParticipantInEventResponse> response = registrationApplicationService.getEventRegistrations(eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the registrations of the authenticated participant.
     *
     * @param userDetails the authenticated participant
     * @return 200 OK with the list of registrations for the current user
     */
    @GetMapping("/api/v1/user/registrations")
    @PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID participantId = userDetails.getUserId();
        List<RegistrationResponse> response = registrationApplicationService.getMyRegistrations(participantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a registration by its ticket code.
     * Used by organizers and admins for on-site check-in.
     *
     * @param ticketCode the generated ticket code (e.g. "ACSP-4X7K")
     * @return 200 OK with the registration details
     */
    @GetMapping("/api/v1/registrations/{ticketCode}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<ParticipantInEventResponse> getRegistrationByTicketCode(
            @PathVariable String ticketCode
    ) {
        ParticipantInEventResponse response = registrationApplicationService.getRegistrationByTicketCode(ticketCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks the race kit as picked up for the registration identified by the given ticket code.
     * Used by organizers and admins at the kit collection point.
     *
     * @param ticketCode the generated ticket code
     * @return 200 OK with the updated registration details
     */
    @PutMapping("/api/v1/registrations/{ticketCode}/kit-pickup")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<ParticipantInEventResponse> markKitPickedUp(
            @PathVariable String ticketCode
    ) {
        ParticipantInEventResponse response = registrationApplicationService.markKitPickedUp(ticketCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/user/registrations/{registrationId}/resend-ticket")
    @PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
    public ResponseEntity<Void> resendTicket(
            @PathVariable UUID registrationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        registrationApplicationService.resendTicketEmail(registrationId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/user/registrations/{registrationId}/ticket")
    @PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
    public ResponseEntity<byte[]> downloadTicket(
            @PathVariable UUID registrationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        byte[] pdf = registrationApplicationService.generateTicketPdf(registrationId, userDetails.getUserId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"boleto-" + registrationId + ".pdf\"")
                .body(pdf);
    }

    private boolean isAdminOrOrganizer(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ORGANIZER"));
    }
}
