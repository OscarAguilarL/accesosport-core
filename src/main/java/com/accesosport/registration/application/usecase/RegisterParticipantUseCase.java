package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCategory;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.repository.EventCapacityRepository;
import com.accesosport.event.domain.repository.EventCategoryRepository;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.dto.RegisterParticipantCommand;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.domain.events.RegistrationConfirmedEvent;
import com.accesosport.registration.domain.exception.DuplicateRegistrationException;
import com.accesosport.registration.domain.exception.NoCapacityException;
import com.accesosport.registration.domain.exception.RegistrationNotOpenException;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.model.RegistrationStatus;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class RegisterParticipantUseCase extends UseCase<RegisterParticipantCommand, RegistrationResponse> {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final EventModalityRepository eventModalityRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventCapacityRepository eventCapacityRepository;

    private static final DateTimeFormatter WAIVER_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    protected RegistrationResponse internalExecute(RegisterParticipantCommand command) {
        if (!command.waiverAccepted()) {
            throw new IllegalArgumentException("Debes aceptar el deslinde de responsabilidad para inscribirte.");
        }

        if (command.participantEmail() == null || command.participantEmail().isBlank()) {
            throw new IllegalArgumentException("El email del participante es requerido.");
        }
        if (command.participantFirstName() == null || command.participantFirstName().isBlank()) {
            throw new IllegalArgumentException("El nombre del participante es requerido.");
        }
        if (command.participantLastName() == null || command.participantLastName().isBlank()) {
            throw new IllegalArgumentException("El apellido del participante es requerido.");
        }

        if (command.participantId() != null) {
            Optional<Registration> existing = registrationRepository.findNonCancelledByEventIdAndParticipantId(
                    command.eventId(), command.participantId());
            if (existing.isPresent()) {
                if (existing.get().getStatus() == RegistrationStatus.PENDING_PAYMENT) {
                    return RegistrationResponse.from(existing.get());
                }
                throw new DuplicateRegistrationException(command.eventId(), command.participantId());
            }
        }

        Optional<Registration> existingByEmail = registrationRepository.findNonCancelledByEventIdAndParticipantEmail(
                command.eventId(), command.participantEmail());
        if (existingByEmail.isPresent()) {
            if (existingByEmail.get().getStatus() == RegistrationStatus.PENDING_PAYMENT) {
                return RegistrationResponse.from(existingByEmail.get());
            }
            throw new DuplicateRegistrationException(command.eventId(), command.participantId());
        }

        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new RegistrationNotOpenException(command.eventId()));

        if (command.modalityId() == null) {
            throw new IllegalArgumentException("Este evento requiere seleccionar una modalidad");
        }

        List<EventModality> modalities = eventModalityRepository.findByEventId(command.eventId());
        EventModality modality = modalities.stream()
                .filter(m -> m.getId().equals(command.modalityId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Modalidad no encontrada para este evento"));

        int reserved = eventCapacityRepository.reserveIfAvailable(command.eventId());
        if (reserved == 0) {
            if (!event.getStatus().acceptsRegistrations()) {
                throw new RegistrationNotOpenException(command.eventId());
            }
            throw new NoCapacityException(command.eventId());
        }

        eventModalityRepository.incrementRegisteredCount(modality.getId());

        UUID categoryId = null;
        if (command.categoryId() != null) {
            List<EventCategory> categories = eventCategoryRepository.findByEventId(command.eventId());
            EventCategory category = categories.stream()
                    .filter(c -> c.getId().equals(command.categoryId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada para este evento"));
            categoryId = category.getId();
        }

        LocalDateTime waiverAcceptedAt = LocalDateTime.now();
        String waiverText = interpolateWaiver(event, command.participantFirstName(), command.participantLastName(), waiverAcceptedAt);

        boolean wantsShirt = command.wantsShirt();
        BigDecimal price = (!wantsShirt && modality.getPriceWithoutShirt() != null)
                ? modality.getPriceWithoutShirt()
                : modality.getPrice();
        Registration registration;

        if (price.compareTo(BigDecimal.ZERO) == 0) {
            registration = Registration.create(command.eventId(), command.participantId(), modality.getId(), categoryId,
                    RegistrationStatus.CONFIRMED, waiverAcceptedAt, waiverText, wantsShirt,
                    command.participantEmail(), command.participantFirstName(), command.participantLastName(),
                    command.participantPhone(), command.shirtSize(), command.bloodType(),
                    command.emergencyContactName(), command.emergencyContactPhone(), command.medicalConditions());
            registrationRepository.save(registration);
            domainEventPublisher.publish(new RegistrationConfirmedEvent(
                    registration.getId(),
                    registration.getEventId(),
                    registration.getParticipantId(),
                    registration.getTicketCode(),
                    null
            ));
        } else {
            registration = Registration.create(command.eventId(), command.participantId(), modality.getId(), categoryId,
                    RegistrationStatus.PENDING_PAYMENT, waiverAcceptedAt, waiverText, wantsShirt,
                    command.participantEmail(), command.participantFirstName(), command.participantLastName(),
                    command.participantPhone(), command.shirtSize(), command.bloodType(),
                    command.emergencyContactName(), command.emergencyContactPhone(), command.medicalConditions());
            registrationRepository.save(registration);
        }

        return RegistrationResponse.from(registration);
    }

    private String interpolateWaiver(Event event, String firstName, String lastName, LocalDateTime acceptedAt) {
        String template = event.getWaiverTemplate();
        if (template == null || template.isBlank()) {
            template = com.accesosport.event.domain.model.Event.DEFAULT_WAIVER_TEMPLATE;
        }

        String participantFullName = (firstName + " " + lastName).trim();
        if (participantFullName.isBlank()) participantFullName = "Participante";

        String eventName = event.getName() != null ? event.getName() : "";
        String eventDate = event.getEventDate() != null
                ? event.getEventDate().format(WAIVER_DATE_FORMATTER) : "";

        return template
                .replace("{participantFullName}", participantFullName)
                .replace("{eventName}", eventName)
                .replace("{eventDate}", eventDate)
                .replace("{waiverAcceptedAt}", acceptedAt.format(WAIVER_DATE_FORMATTER));
    }
}
