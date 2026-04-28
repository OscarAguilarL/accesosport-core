package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.events.EventCancelledEvent;
import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class CancelEventUseCase extends UseCase<CancelEventUseCase.CancelEventCommand, CancelEventUseCase.CancelEventResult> {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    protected CancelEventResult internalExecute(CancelEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        if (command.requesterId() != null && !event.getCreatedBy().getId().equals(command.requesterId())) {
            throw new EventAccessDeniedException();
        }

        List<UUID> affectedIds = registrationRepository.findConfirmedByEventId(command.eventId())
                .stream()
                .map(r -> r.getId())
                .toList();

        event.cancel();

        Event savedEvent = eventRepository.save(event);

        domainEventPublisher.publish(
                new EventCancelledEvent(savedEvent, command.reason(), affectedIds)
        );

        return new CancelEventResult(savedEvent);
    }

    public record CancelEventCommand(UUID eventId, String reason, UUID requesterId) {
    }

    public record CancelEventResult(Event canceledEvent) {
    }
}
