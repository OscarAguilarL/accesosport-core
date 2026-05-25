package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class PublishEventUseCase extends UseCase<PublishEventUseCase.PublishEventCommand, PublishEventUseCase.PublishEventResult> {

    private final EventRepository eventRepository;
    private final EventModalityRepository eventModalityRepository;

    @Override
    protected PublishEventResult internalExecute(PublishEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        if (command.requesterId() != null && !event.getCreatedBy().getId().equals(command.requesterId())) {
            throw new EventAccessDeniedException();
        }

        if (eventModalityRepository.findByEventId(command.eventId()).isEmpty()) {
            throw new IllegalStateException("El evento debe tener al menos una modalidad antes de publicarse");
        }

        event.publish();

        return new PublishEventResult(eventRepository.save(event));
    }

    public record PublishEventCommand(UUID eventId, UUID requesterId) {}

    public record PublishEventResult(Event event) {}
}
