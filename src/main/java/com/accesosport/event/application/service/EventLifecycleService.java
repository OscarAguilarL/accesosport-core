package com.accesosport.event.application.service;

import com.accesosport.event.domain.events.RegistrationClosedEvent;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLifecycleService {

    private final EventRepository eventRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Value("${app.scheduler.event-lifecycle.complete-after-hours:12}")
    private int completeAfterHours;

    @Transactional
    public void autoOpenRegistrations() {
        List<Event> events = eventRepository.findEventsReadyToOpenRegistration(LocalDateTime.now());
        events.forEach(event -> {
            try {
                event.openRegistration();
                eventRepository.save(event);
                log.info("[Scheduler] Opened registration for event {}", event.getId());
            } catch (Exception e) {
                log.error("[Scheduler] Failed to open registration for event {}: {}", event.getId(), e.getMessage());
            }
        });
    }

    @Transactional
    public void autoCloseRegistrations() {
        List<Event> events = eventRepository.findEventsReadyToCloseRegistration(LocalDateTime.now());
        events.forEach(event -> {
            try {
                event.closeRegistration();
                eventRepository.save(event);
                domainEventPublisher.publish(new RegistrationClosedEvent(event.getId()));
                log.info("[Scheduler] Published RegistrationClosedEvent for event {}", event.getId());
                log.info("[Scheduler] Closed registration for event {}", event.getId());
            } catch (Exception e) {
                log.error("[Scheduler] Failed to close registration for event {}: {}", event.getId(), e.getMessage());
            }
        });
    }

    @Transactional
    public void autoBeginEvents() {
        List<Event> events = eventRepository.findEventsReadyToBegin(LocalDateTime.now());
        events.forEach(event -> {
            try {
                event.begin();
                eventRepository.save(event);
                log.info("[Scheduler] Started event {}", event.getId());
            } catch (Exception e) {
                log.error("[Scheduler] Failed to begin event {}: {}", event.getId(), e.getMessage());
            }
        });
    }

    @Transactional
    public void autoCompleteEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(completeAfterHours);
        List<Event> events = eventRepository.findEventsReadyToComplete(threshold);
        events.forEach(event -> {
            try {
                event.complete();
                eventRepository.save(event);
                log.info("[Scheduler] Completed event {}", event.getId());
            } catch (Exception e) {
                log.error("[Scheduler] Failed to complete event {}: {}", event.getId(), e.getMessage());
            }
        });
    }
}
