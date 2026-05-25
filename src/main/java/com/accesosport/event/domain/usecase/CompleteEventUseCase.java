package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CompleteEventUseCase extends UseCase<CompleteEventUseCase.CompleteEventCommand, CompleteEventUseCase.CompleteEventResult> {

    private final EventRepository eventRepository;

    @Override
    protected CompleteEventResult internalExecute(CompleteEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        if (command.requesterId() != null && !event.getCreatedBy().getId().equals(command.requesterId())) {
            throw new EventAccessDeniedException();
        }

        event.complete();

        Event saved = eventRepository.save(event);
        return new CompleteEventResult(saved);
    }

    public record CompleteEventCommand(UUID eventId, UUID requesterId) {}

    public record CompleteEventResult(Event event) {}
}
