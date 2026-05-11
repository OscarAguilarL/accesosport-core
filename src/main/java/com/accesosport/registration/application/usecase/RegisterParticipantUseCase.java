package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventModality;
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
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class RegisterParticipantUseCase extends UseCase<RegisterParticipantCommand, RegistrationResponse> {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final EventModalityRepository eventModalityRepository;

    @Override
    protected RegistrationResponse internalExecute(RegisterParticipantCommand command) {
        if (registrationRepository.existsByEventIdAndParticipantId(command.eventId(), command.participantId())) {
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

        int reserved = eventModalityRepository.reserveIfAvailable(modality.getId());
        if (reserved == 0) {
            if (!event.getStatus().acceptsRegistrations()) {
                throw new RegistrationNotOpenException(command.eventId());
            }
            throw new NoCapacityException(command.eventId());
        }

        BigDecimal price = modality.getPrice();
        Registration registration;

        if (price.compareTo(BigDecimal.ZERO) == 0) {
            registration = Registration.create(command.eventId(), command.participantId(), modality.getId(), RegistrationStatus.CONFIRMED);
            registrationRepository.save(registration);
            domainEventPublisher.publish(new RegistrationConfirmedEvent(
                    registration.getId(),
                    registration.getEventId(),
                    registration.getParticipantId(),
                    registration.getTicketCode(),
                    null
            ));
        } else {
            registration = Registration.create(command.eventId(), command.participantId(), modality.getId(), RegistrationStatus.PENDING_PAYMENT);
            registrationRepository.save(registration);
        }

        return RegistrationResponse.from(registration);
    }
}
