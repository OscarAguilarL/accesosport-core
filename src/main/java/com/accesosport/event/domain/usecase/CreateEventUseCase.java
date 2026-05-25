package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.model.RegistrationPeriod;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.exception.UserNotFoundException;
import com.accesosport.user.domain.model.RoleEnumeration;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class CreateEventUseCase extends UseCase<CreateEventUseCase.CreateEventCommand, CreateEventUseCase.CreateEventResult> {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventModalityRepository eventModalityRepository;

    @Override
    public CreateEventResult internalExecute(CreateEventCommand command) {
        User organizer = userRepository.findById(command.createdByUserId())
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.Events.EVENT_VALIDATION_ORGANIZER_NOT_FOUND));

        if (!organizer.hasRole(RoleEnumeration.ROLE_ORGANIZER)) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_USER_NOT_ORGANIZER);
        }

        Location location = Location.of(
                command.place(), command.city(), command.country()
        );
        RegistrationPeriod registrationPeriod = RegistrationPeriod.of(
                command.registrationStart(), command.registrationEnd()
        );

        Event event = Event.create(
                command.name(),
                command.description(),
                command.eventDate(),
                location,
                registrationPeriod,
                organizer
        );

        Event savedEvent = eventRepository.save(event);

        List<EventModality> modalities = command.modalities().stream()
                .map(m -> EventModality.create(
                        savedEvent.getId(),
                        m.name(),
                        m.distance(),
                        m.distanceUnit(),
                        m.price(),
                        m.capacity()
                ))
                .map(eventModalityRepository::save)
                .toList();

        return new CreateEventResult(savedEvent, modalities);
    }

    public record ModalityData(
            String name,
            BigDecimal distance,
            com.accesosport.event.domain.model.DistanceUnit distanceUnit,
            BigDecimal price,
            int capacity
    ) {}

    public record CreateEventCommand(
            String name,
            String description,
            LocalDateTime eventDate,
            String place,
            String city,
            String country,
            LocalDateTime registrationStart,
            LocalDateTime registrationEnd,
            List<ModalityData> modalities,
            UUID createdByUserId
    ) {}

    public record CreateEventResult(Event event, List<EventModality> modalities) {}
}
