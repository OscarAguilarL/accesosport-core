package com.accesosport.user.presentation.rest;

import com.accesosport.auth.infrastructure.security.CustomUserDetails;
import com.accesosport.user.application.dto.CreateOrganizerProfileRequest;
import com.accesosport.user.application.dto.CreateParticipantProfileRequest;
import com.accesosport.user.application.dto.OrganizerProfileResponse;
import com.accesosport.user.application.dto.OrganizerProfileWithTokenResponse;
import com.accesosport.user.application.dto.ParticipantProfileResponse;
import com.accesosport.user.application.dto.ParticipantProfileWithTokenResponse;
import com.accesosport.user.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * The ProfileController handles HTTP requests related to organizer and participant profiles.
 * It provides endpoints for creating and retrieving profiles associated with the authenticated user.
 */
@RestController
@RequestMapping("/api/v1/user/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;

    /**
     * Creates a new organizer profile based on the provided details and associates it with the authenticated user.
     *
     * @param request     The request object containing the details needed to create an organizer profile.
     *                    Must contain valid inputs according to the specified constraints in the CreateOrganizerProfileRequest class.
     * @param userDetails The authenticated user's details used to associate the organizer profile with a specific user.
     *                    Contains information such as the user's unique identifier.
     * @return A ResponseEntity containing the created OrganizerProfileResponse object and an HTTP status of 201 (Created)
     * if the profile is successfully created.
     */
    @PostMapping("/organizer")
    public ResponseEntity<OrganizerProfileWithTokenResponse> createOrganizerProfile(
            @Valid @RequestBody CreateOrganizerProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID organizerId = userDetails.getUserId();
        OrganizerProfileWithTokenResponse response = userService.createOrganizerProfile(organizerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the profile of the organizer associated with the currently authenticated user.
     *
     * @param userDetails The authenticated user's details, which include the unique identifier.
     *                    Used to identify the associated organizer profile.
     * @return A ResponseEntity containing the OrganizerProfileResponse object and an HTTP status of 200 (OK)
     * if the profile is successfully retrieved.
     */
    @GetMapping("/organizer")
    public ResponseEntity<OrganizerProfileResponse> getOrganizerProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID organizerId = userDetails.getUserId();
        OrganizerProfileResponse response = userService.getOrganizerProfile(organizerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new participant profile based on the provided details and associates it with the authenticated user.
     *
     * @param request     The request object containing the details needed to create a participant profile.
     *                    Must contain valid inputs according to the specified constraints in the CreateParticipantProfileRequest class.
     * @param userDetails The authenticated user's details used to associate the participant profile with a specific user.
     *                    Contains information such as the user's unique identifier.
     * @return A ResponseEntity containing the created ParticipantProfileResponse object and an HTTP status of 201 (Created)
     * if the profile is successfully created.
     */
    @PostMapping("/participant")
    public ResponseEntity<ParticipantProfileWithTokenResponse> createParticipantProfile(
            @Valid @RequestBody CreateParticipantProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID participantId = userDetails.getUserId();
        ParticipantProfileWithTokenResponse response = userService.createParticipantProfile(participantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/participant")
    @PreAuthorize("hasAuthority('ROLE_PARTICIPANT')")
    public ResponseEntity<ParticipantProfileResponse> updateParticipantProfile(
            @Valid @RequestBody CreateParticipantProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID participantId = userDetails.getUserId();
        ParticipantProfileResponse response = userService.updateParticipantProfile(participantId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profile of the participant associated with the currently authenticated user.
     *
     * @param userDetails The authenticated user's details, which include the unique identifier.
     *                    Used to identify the associated participant profile.
     * @return A ResponseEntity containing the ParticipantProfileResponse object and an HTTP status of 200 (OK)
     * if the profile is successfully retrieved.
     */
    @GetMapping("/participant")
    public ResponseEntity<ParticipantProfileResponse> getParticipantProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID participantId = userDetails.getUserId();
        ParticipantProfileResponse response = userService.getParticipantProfile(participantId);
        return ResponseEntity.ok(response);
    }
}
