package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;

import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * UseCase responsible for listing events by organizer.
 */
@AllArgsConstructor
public class ListEventsByOrganizerUseCase extends UseCase<
        ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand,
        ListEventsByOrganizerUseCase.ListEventsByOrganizerResult
        > {

    /**
     * Repository responsible for event persistence.
     */
    public EventRepository eventRepository;

    @Override
    protected ListEventsByOrganizerResult internalExecute(ListEventsByOrganizerCommand command) {
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
