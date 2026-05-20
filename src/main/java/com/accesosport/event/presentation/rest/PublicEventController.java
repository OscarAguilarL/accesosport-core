package com.accesosport.event.presentation.rest;

import com.accesosport.event.application.dto.*;
import com.accesosport.event.application.service.EventApplicationService;
import com.accesosport.event.application.service.EventCategoryApplicationService;
import com.accesosport.event.application.service.EventModalityApplicationService;
import com.accesosport.image.application.dto.EventImageResponse;
import com.accesosport.image.application.service.ImageApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventApplicationService eventApplicationService;
    private final EventModalityApplicationService eventModalityApplicationService;
    private final EventCategoryApplicationService eventCategoryApplicationService;
    private final ImageApplicationService imageApplicationService;

    @GetMapping("/published")
    public ResponseEntity<List<EventSummaryResponse>> listPublishedEvents() {
        return ResponseEntity.ok(eventApplicationService.listPublishedEvents());
    }

    @GetMapping("/available")
    public ResponseEntity<List<EventSummaryResponse>> listAvailableEvents() {
        return ResponseEntity.ok(eventApplicationService.listAvailableEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventApplicationService.getEvent(eventId));
    }

    @GetMapping("/{eventId}/modalities")
    public ResponseEntity<List<ModalityResponse>> listModalities(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventModalityApplicationService.listModalities(eventId));
    }

    @GetMapping("/{eventId}/categories")
    public ResponseEntity<List<CategoryResponse>> listCategories(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventCategoryApplicationService.listCategories(eventId));
    }

    @GetMapping("/{eventId}/images")
    public ResponseEntity<List<EventImageResponse>> listImages(@PathVariable UUID eventId) {
        return ResponseEntity.ok(imageApplicationService.getEventGallery(eventId));
    }
}
