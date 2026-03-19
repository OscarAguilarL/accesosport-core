package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;

import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * PublishEventUseCase handles the publishing of events,
 * ensuring that necessary business rules and validations are applied during the process.
 */
@AllArgsConstructor
public class PublishEventUseCase extends UseCase<PublishEventUseCase.PublishEventCommand, PublishEventUseCase.PublishEventResult> {

    private final EventRepository eventRepository;

    @Override
    protected PublishEventResult internalExecute(PublishEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        event.publish();

        Event publishedEvent = eventRepository.save(event);

        return new PublishEventResult(publishedEvent);
    }

    /**
     * Represents a command to publish an event, encapsulating the event identifier.
     *
     * @param eventId Event identifier to be published
     */
    public record PublishEventCommand(UUID eventId) {
    }

    /**
     * Represents the result of publishing an event.
     *
     * @param event Published event entity
     */
    public record PublishEventResult(Event event) {

    }
}
