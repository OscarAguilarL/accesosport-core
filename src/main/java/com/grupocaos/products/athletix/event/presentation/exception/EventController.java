package com.grupocaos.products.athletix.event.presentation.exception;

import com.grupocaos.products.athletix.auth.infrastructure.security.CustomUserDetails;
import com.grupocaos.products.athletix.event.application.dto.CreateEventRequest;
import com.grupocaos.products.athletix.event.application.dto.EventResponse;
import com.grupocaos.products.athletix.event.application.service.EventApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * The {@code EventController} class exposes REST endpoints for managing events.
 * This controller handles operations such as creating new events. It provides
 * APIs that are secured and limited to specific roles, such as "ROLE_ORGANIZER"
 * and "ROLE_ADMIN".
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventApplicationService eventApplicationService;

    /**
     * Creates a new event based on the provided request data.
     * Only users with the "ROLE_ORGANIZER" or "ROLE_ADMIN" authority
     * are authorized to perform this operation.
     *
     * @param request     the detail of the event to be created; contains fields
     *                    such as name, description, event date, location, race type,
     *                    distance, price, registration period, and maximum participants
     * @param userDetails the authentication details of the currently logged-in user,
     *                    which includes the organizer's identifier
     * @return a ResponseEntity containing the created event's details wrapped in an EventResponse object
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID organizerId = userDetails.getUserId();
        EventResponse eventResponse = eventApplicationService.createEvent(request, organizerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);
    }
}
