package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Distance;
import com.accesosport.event.domain.model.DistanceUnit;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCapacity;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.model.RaceType;
import com.accesosport.event.domain.model.RegistrationPeriod;
import com.accesosport.event.domain.repository.EventCapacityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.Objects;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
public class UpdateEventUseCase extends UseCase<UpdateEventUseCase.UpdateEventCommand, UpdateEventUseCase.UpdateEventResult> {

    private final EventRepository eventRepository;
    private final EventCapacityRepository eventCapacityRepository;

    @Override
    protected UpdateEventResult internalExecute(UpdateEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        if (command.requesterId() != null
                && !event.getCreatedBy().getId().equals(command.requesterId())) {
            throw new EventAccessDeniedException();
        }

        EventCapacity capacity = eventCapacityRepository.findByEventId(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        // Merge scalars
        String mergedName             = command.name()            != null ? command.name()            : event.getName();
        String mergedDescription      = command.description()     != null ? command.description()     : event.getDescription();
        LocalDateTime mergedEventDate = command.eventDate()       != null ? command.eventDate()       : event.getEventDate();
        RaceType mergedRaceType       = command.raceType()        != null ? command.raceType()        : event.getRaceType();
        BigDecimal mergedPrice        = command.price()           != null ? command.price()           : event.getPrice();
        Integer mergedMaxParticipants = command.maxParticipants() != null ? command.maxParticipants() : capacity.getMaxCapacity();

        // Merge Location
        Location currentLoc     = event.getLocation();
        String mergedPlace      = command.place()     != null ? command.place()     : currentLoc.place();
        String mergedCity       = command.city()      != null ? command.city()      : currentLoc.city();
        String mergedCountry    = command.country()   != null ? command.country()   : currentLoc.country();
        Double mergedLat        = command.latitude()  != null ? command.latitude()  : currentLoc.latitude();
        Double mergedLon        = command.longitude() != null ? command.longitude() : currentLoc.longitude();
        Location mergedLocation = Location.of(mergedPlace, mergedCity, mergedCountry, mergedLat, mergedLon);

        // Merge Distance
        Distance currentDist        = event.getDistance();
        BigDecimal mergedDistVal    = command.distance()     != null ? command.distance()     : currentDist.value();
        DistanceUnit mergedDistUnit = command.distanceUnit() != null ? command.distanceUnit() : currentDist.unit();
        Distance mergedDistance     = Distance.of(mergedDistVal, mergedDistUnit);

        // Merge RegistrationPeriod
        RegistrationPeriod currentPeriod = event.getRegistrationPeriod();
        LocalDateTime mergedRegStart     = command.registrationStart() != null ? command.registrationStart() : currentPeriod.start();
        LocalDateTime mergedRegEnd       = command.registrationEnd()   != null ? command.registrationEnd()   : currentPeriod.end();
        RegistrationPeriod mergedPeriod  = RegistrationPeriod.of(mergedRegStart, mergedRegEnd);

        event.update(
                mergedName,
                mergedDescription,
                mergedEventDate,
                mergedLocation,
                mergedRaceType,
                mergedDistance,
                mergedPrice,
                mergedPeriod
        );

        Event savedEvent = eventRepository.save(event);

        if (!Objects.equals(capacity.getMaxCapacity(), mergedMaxParticipants)) {
            capacity.updateMaxCapacity(mergedMaxParticipants);
            eventCapacityRepository.save(capacity);
        }

        return new UpdateEventResult(savedEvent);
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
            RaceType raceType,
            BigDecimal distance,
            DistanceUnit distanceUnit,
            BigDecimal price,
            LocalDateTime registrationStart,
            LocalDateTime registrationEnd,
            Integer maxParticipants
    ) {
    }

    public record UpdateEventResult(Event event) {
    }
}
