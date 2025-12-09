package com.grupocaos.products.athletix.event.domain.usecase;

import com.grupocaos.products.athletix.event.domain.exception.EventNotFoundException;
import com.grupocaos.products.athletix.event.domain.model.Event;
import com.grupocaos.products.athletix.event.domain.repository.EventRepository;

import com.grupocaos.products.athletix.shared.use_case.domain.UseCase;
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

        event.openRegistration();

        Event savedEvent = eventRepository.save(event);

        return new OpenRegistrationResult(savedEvent);
    }

    /**
     * @param eventId Event identifier to be opened for registration
     */
    public record OpenRegistrationCommand(UUID eventId) {
    }

    /**
     * @param event Event entity that has been opened for registration.
     */
    public record OpenRegistrationResult(Event event) {
    }
}
