package com.grupocaos.products.athletix.event.domain.usecase;

import com.grupocaos.products.athletix.event.domain.exception.EventNotFoundException;
import com.grupocaos.products.athletix.event.domain.model.Event;
import com.grupocaos.products.athletix.event.domain.repository.EventRepository;

import com.grupocaos.products.athletix.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * UseCase responsible for cancelling an event.
 */
@AllArgsConstructor
public class CancelEventUseCase extends UseCase<CancelEventUseCase.CancelEventCommand, CancelEventUseCase.CancelEventResult> {

    private final EventRepository eventRepository;

    @Override
    protected CancelEventResult internalExecute(CancelEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        event.cancel();

        Event savedEvent = eventRepository.save(event);

        return new CancelEventResult(savedEvent);
    }

    /**
     * Represents a command to cancel an event, encapsulating the event identifier and cancellation reason.
     *
     * @param eventId unique identifier of the event to be canceled
     * @param reason  cancellation reason provided by the organizer
     */
    public record CancelEventCommand(UUID eventId, String reason) {
    }

    /**
     * Represents the result of cancelling an event.
     *
     * @param canceledEvent Event entity that has been canceled.
     */
    public record CancelEventResult(Event canceledEvent) {
    }
}
