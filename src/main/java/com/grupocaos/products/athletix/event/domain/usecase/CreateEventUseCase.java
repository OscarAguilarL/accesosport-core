package com.grupocaos.products.athletix.event.domain.usecase;

import com.grupocaos.products.athletix.event.domain.model.Distance;
import com.grupocaos.products.athletix.event.domain.model.DistanceUnit;
import com.grupocaos.products.athletix.event.domain.model.Event;
import com.grupocaos.products.athletix.event.domain.model.Location;
import com.grupocaos.products.athletix.event.domain.model.RaceType;
import com.grupocaos.products.athletix.event.domain.model.RegistrationPeriod;
import com.grupocaos.products.athletix.event.domain.repository.EventRepository;
import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;
import com.grupocaos.products.athletix.shared.domain.usecase.UseCase;
import com.grupocaos.products.athletix.user.domain.exception.UserNotFoundException;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CreateEventUseCase handles the creation of new events, ensuring that
 * necessary business rules and validations are applied during the process.
 * It extends the AbstractUseCase to leverage standardized use case execution flow.
 * <p>
 * This use case primarily interacts with the EventRepository for event persistence
 * and the UserRepository to validate the organizer's credentials and permissions.
 * <p>
 * Key validations include:
 * <ul>
 *     <li>Ensuring that the organizer exists.</li>
 *     <li>Checking that the organizer has the correct role to create events.</li>
 *     <li>Validating the location, distance, and registration period details.</li>
 * </ul>
 * <p>
 * The output of this use case is a CreateEventResult, which encapsulates the successfully
 * created Event entity.
 */
@AllArgsConstructor
public class CreateEventUseCase extends UseCase<CreateEventUseCase.CreateEventCommand, CreateEventUseCase.CreateEventResult> {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public CreateEventResult internalExecute(CreateEventCommand command) {
        User organizer = userRepository.findById(command.createdByUserId())
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.Events.EVENT_VALIDATION_ORGANIZER_NOT_FOUND));

        if (!organizer.hasRole(RoleEnumeration.ROLE_ORGANIZER)) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_USER_NOT_ORGANIZER);
        }

        Location location = Location.of(
                command.place(),
                command.city(),
                command.country(),
                command.latitude(),
                command.longitude()
        );

        Distance distance = Distance.of(
                command.distance(),
                command.distanceUnit()
        );

        RegistrationPeriod registrationPeriod = RegistrationPeriod.of(command.registrationStart(), command.registrationEnd());

        Event event = Event.create(
                command.name(),
                command.description(),
                command.eventDate(),
                location,
                command.raceType(),
                distance,
                command.price(),
                registrationPeriod,
                command.maxParticipants(),
                organizer
        );

        Event savedEvent = eventRepository.save(event);

        return new CreateEventResult(savedEvent);
    }

    /**
     * Represents a command to create a new event, encapsulating all necessary details about the event.
     * <p>
     * This command is typically used in the context of a use case or business logic to create an event
     * in the system. The information provided in this command includes:
     *
     * <ul>
     *     <li>Event metadata such as name and description.</li>
     *     <li>Event scheduling information like date and registration period.</li>
     *     <li>Location details including place, city, country, latitude, and longitude.</li>
     *     <li>Race-related information such as type, distance, and distance unit.</li>
     *     <li>Registration and pricing specifics.</li>
     *     <li>Constraints like maximum number of participants.</li>
     *     <li>Identifier of the organizer/user creating the event.</li>
     * </ul>
     * <p>
     * This class is implemented as a record, meaning it is immutable, thread-safe, and primarily
     * used for data transfer purposes.
     */
    public record CreateEventCommand(
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
            Integer maxParticipants,
            UUID createdByUserId
    ) {
    }

    /**
     * Represents the result of creating an event in the system.
     * <p>
     * This record encapsulates the event entity that has been successfully created
     * and stored through the execution of the business logic.
     * <p>
     * The returned event includes all configured properties, such as its unique
     * identifier, name, description, date, location, race details, registration
     * terms, and any other relevant information. Additionally, it reflects the
     * initial state of the event after creation, before any subsequent actions
     * like publishing or opening registration.
     */
    public record CreateEventResult(Event event) {
    }
}
