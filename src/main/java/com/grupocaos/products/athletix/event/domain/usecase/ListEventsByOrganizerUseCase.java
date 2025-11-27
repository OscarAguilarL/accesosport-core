package com.grupocaos.products.athletix.event.domain.usecase;

import com.grupocaos.products.athletix.event.domain.model.Event;
import com.grupocaos.products.athletix.event.domain.repository.EventRepository;
import com.grupocaos.products.athletix.shared.use_case.domain.AbstractUseCase;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * UseCase responsible for listing events by organizer.
 */
@AllArgsConstructor
public class ListEventsByOrganizerUseCase extends AbstractUseCase<
        ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand,
        ListEventsByOrganizerUseCase.ListEventsByOrganizerResult
        > {

    /**
     * Repository responsible for event persistence.
     */
    public EventRepository eventRepository;

    @Override
    protected ListEventsByOrganizerResult doExecute(ListEventsByOrganizerCommand command) {
        List<Event> eventsByOrganizer = eventRepository.findByOrganizerId(command.organizerId());

        return new ListEventsByOrganizerResult(eventsByOrganizer);
    }

    /**
     * @param organizerId Identifier of the organizer to be queried for events.
     */
    public record ListEventsByOrganizerCommand(UUID organizerId) {
    }

    /**
     * @param events List of events that belong to the organizer identified by the command.
     */
    public record ListEventsByOrganizerResult(List<Event> events) {
    }
}
