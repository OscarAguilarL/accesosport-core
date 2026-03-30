package com.accesosport.event.presentation.rest;

import com.accesosport.auth.infrastructure.security.CustomUserDetails;
import com.accesosport.event.application.dto.CreateEventRequest;
import com.accesosport.event.application.dto.EventResponse;
import com.accesosport.event.application.dto.EventSummaryResponse;
import com.accesosport.event.application.dto.UpdateEventRequest;
import com.accesosport.event.application.service.EventApplicationService;
import com.accesosport.event.domain.model.EventStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    /**
     * Retrieves the details of an event specified by its unique identifier.
     *
     * @param eventId the unique identifier of the event to retrieve
     * @return a ResponseEntity containing the event details wrapped in an EventResponse object
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID eventId) {
        EventResponse eventResponse = eventApplicationService.getEvent(eventId);
        return ResponseEntity.ok(eventResponse);
    }

    /**
     * Retrieves a list of events filtered by their status or a list of upcoming events if no status is specified.
     *
     * @param eventStatus the status of the events to filter; if null, this method returns upcoming events
     * @return a ResponseEntity containing a list of {@code EventSummaryResponse} objects representing the filtered events
     * or upcoming events
     */
    @GetMapping
    public ResponseEntity<List<EventSummaryResponse>> listEvents(@RequestParam(required = false) EventStatus eventStatus) {
        List<EventSummaryResponse> eventSummaryResponses = eventStatus != null
                ? eventApplicationService.ListEventsByStatus(eventStatus)
                : eventApplicationService.listUpcomingEvents();

        return ResponseEntity.ok(eventSummaryResponses);
    }

    /**
     * Retrieves a list of events that are currently available for registration.
     *
     * @return a ResponseEntity containing a list of {@code EventSummaryResponse} objects
     * representing the currently available events
     */
    @GetMapping("/available")
    public ResponseEntity<List<EventSummaryResponse>> listAvailableEvents() {
        List<EventSummaryResponse> eventSummaryResponses = eventApplicationService.listAvailableEvents();

        return ResponseEntity.ok(eventSummaryResponses);
    }

    /**
     * Retrieves a list of events organized by the currently authenticated organizer.
     * Only users with the "ROLE_ORGANIZER" or "ROLE_ADMIN" authority are authorized to perform this operation.
     *
     * @param userDetails the authentication details of the currently logged-in user,
     *                    which includes the organizer's unique identifier
     * @return a ResponseEntity containing a list of {@code EventSummaryResponse} objects
     * representing the events organized by the authenticated user
     */
    @GetMapping("/my-events")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<List<EventSummaryResponse>> listMyEvents(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<EventSummaryResponse> eventSummaryResponses = eventApplicationService.listEventsByOrganizerId(userDetails.getUserId());
        return ResponseEntity.ok(eventSummaryResponses);
    }

    /**
     * Publishes the specified event, making it publicly visible and available for interaction.
     * Only users with the "ROLE_ORGANIZER" or "ROLE_ADMIN" authority are authorized to perform this operation.
     *
     * @param eventId the unique identifier of the event to be published
     * @return a ResponseEntity containing the published event's details wrapped in an EventResponse object
     */
    @PatchMapping("/{eventId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID requesterId = isAdmin(userDetails) ? null : userDetails.getUserId();
        EventResponse eventResponse = eventApplicationService.updateEvent(eventId, request, requesterId);
        return ResponseEntity.ok(eventResponse);
    }

    @PutMapping("/{eventId}/publish")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID requesterId = isAdmin(userDetails) ? null : userDetails.getUserId();
        EventResponse eventResponse = eventApplicationService.publishEvent(eventId, requesterId);
        return ResponseEntity.ok(eventResponse);
    }

    /**
     * Opens the registration for the specified event, allowing participants to register.
     * Only users with the "ROLE_ORGANIZER" or "ROLE_ADMIN" authority are authorized to perform this operation.
     *
     * @param eventId the unique identifier of the event for which the registration is to be opened
     * @return a ResponseEntity containing the updated event details wrapped in an EventResponse object
     */
    @PutMapping("/{eventId}/open-registration")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<EventResponse> openRegistration(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID requesterId = isAdmin(userDetails) ? null : userDetails.getUserId();
        EventResponse eventResponse = eventApplicationService.openRegistration(eventId, requesterId);

        return ResponseEntity.ok(eventResponse);
    }

    /**
     * Cancels an event specified by its unique identifier and provides a reason for cancellation.
     * Only users with the "ROLE_ORGANIZER" or "ROLE_ADMIN" authority are authorized to perform this operation.
     *
     * @param eventId the unique identifier of the event to be canceled
     * @param reason  the reason for canceling the event; if not provided, defaults to "Cancelled by organizer"
     * @return a ResponseEntity containing the details of the canceled event wrapped in an EventResponse object
     */
    @DeleteMapping("/{eventId}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<EventResponse> cancelEvent(
            @PathVariable UUID eventId,
            @RequestParam(required = false, defaultValue = "Cancelled by organizer") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID requesterId = isAdmin(userDetails) ? null : userDetails.getUserId();
        EventResponse eventResponse = eventApplicationService.cancelEvent(eventId, reason, requesterId);
        return ResponseEntity.ok(eventResponse);
    }

    private boolean isAdmin(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
