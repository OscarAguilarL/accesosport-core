package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.model.RegistrationPeriod;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
public class UpdateEventUseCase extends UseCase<UpdateEventUseCase.UpdateEventCommand, UpdateEventUseCase.UpdateEventResult> {

    private final EventRepository eventRepository;

    @Override
    protected UpdateEventResult internalExecute(UpdateEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        if (command.requesterId() != null
                && !event.getCreatedBy().getId().equals(command.requesterId())) {
            throw new EventAccessDeniedException();
        }

        String mergedName             = command.name()        != null ? command.name()        : event.getName();
        String mergedDescription      = command.description() != null ? command.description() : event.getDescription();
        LocalDateTime mergedEventDate = command.eventDate()   != null ? command.eventDate()   : event.getEventDate();

        Location currentLoc   = event.getLocation();
        String mergedPlace    = command.place()     != null ? command.place()     : currentLoc.place();
        String mergedCity     = command.city()      != null ? command.city()      : currentLoc.city();
        String mergedCountry  = command.country()   != null ? command.country()   : currentLoc.country();
        Double mergedLat      = command.latitude()  != null ? command.latitude()  : currentLoc.latitude();
        Double mergedLon      = command.longitude() != null ? command.longitude() : currentLoc.longitude();
        Location mergedLocation = Location.of(mergedPlace, mergedCity, mergedCountry, mergedLat, mergedLon);

        RegistrationPeriod currentPeriod = event.getRegistrationPeriod();
        LocalDateTime mergedRegStart = command.registrationStart() != null ? command.registrationStart() : currentPeriod.start();
        LocalDateTime mergedRegEnd   = command.registrationEnd()   != null ? command.registrationEnd()   : currentPeriod.end();
        RegistrationPeriod mergedPeriod = RegistrationPeriod.of(mergedRegStart, mergedRegEnd);

        event.update(mergedName, mergedDescription, mergedEventDate, mergedLocation, mergedPeriod);

        return new UpdateEventResult(eventRepository.save(event));
    }

    public record UpdateEventCommand(
            UUID eventId,
            UUID requesterId,
            String name,
            String description,
            LocalDateTime eventDate,
            String place,
            String city,
            String country,
            Double latitude,
            Double longitude,
            LocalDateTime registrationStart,
            LocalDateTime registrationEnd
    ) {}

    public record UpdateEventResult(Event event) {}
}
