package com.grupocaos.products.athletix.event.domain.usecase;

import com.grupocaos.products.athletix.event.domain.model.Event;
import com.grupocaos.products.athletix.event.domain.repository.EventRepository;
import com.grupocaos.products.athletix.shared.domain.usecase.AbstractUseCase;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * UseCase responsible for listing available events for registration.
 */
@AllArgsConstructor
public class ListAvailableEventsUseCase extends AbstractUseCase<Void, ListAvailableEventsUseCase.ListAvailableEventsResult> {

    private final EventRepository eventRepository;

    @Override
    protected ListAvailableEventsResult doExecute(Void command) {

        List<Event> events = eventRepository.findEventosAvailableForRegistration();

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
