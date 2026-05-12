package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;

import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * UseCase responsible for opening registration for an event.
 */
@AllArgsConstructor
public class OpenRegistrationUseCase extends UseCase<OpenRegistrationUseCase.OpenRegistrationCommand, OpenRegistrationUseCase.OpenRegistrationResult> {

    private final EventRepository eventRepository;

    @Override
    protected OpenRegistrationResult internalExecute(OpenRegistrationCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        if (command.requesterId() != null && !event.getCreatedBy().getId().equals(command.requesterId())) {
            throw new EventAccessDeniedException();
        }

        event.openRegistrationManually();

        Event savedEvent = eventRepository.save(event);

        return new OpenRegistrationResult(savedEvent);
    }

    /**
     * @param eventId Event identifier to be opened for registration
     */
    public record OpenRegistrationCommand(UUID eventId, UUID requesterId) {
    }

    /**
     * @param event Event entity that has been opened for registration.
     */
    public record OpenRegistrationResult(Event event) {
    }
}
