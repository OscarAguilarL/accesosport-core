package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;

import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * UseCase responsible for listing available events for registration.
 */
@AllArgsConstructor
public class ListAvailableEventsUseCase extends UseCase<Void, ListAvailableEventsUseCase.ListAvailableEventsResult> {

    private final EventRepository eventRepository;

    @Override
    protected ListAvailableEventsResult internalExecute(Void command) {

        List<Event> events = eventRepository.findEventsAvailableForRegistration();

        List<Event> availableEvents = events.stream()
                .filter(Event::canRegister)
                .toList();

        return new ListAvailableEventsResult(availableEvents);
    }

    /**
     * Represents the result of listing available events for registration.
     *
     * @param events List of events that are available for registration.
     */
    public record ListAvailableEventsResult(List<Event> events) {
    }
}
