package com.accesosport.event.presentation.rest;

import com.accesosport.event.application.dto.EventSummaryResponse;
import com.accesosport.event.application.service.EventApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventApplicationService eventApplicationService;

    @GetMapping("/published")
    public ResponseEntity<List<EventSummaryResponse>> listPublishedEvents() {
        return ResponseEntity.ok(eventApplicationService.listPublishedEvents());
    }
}
