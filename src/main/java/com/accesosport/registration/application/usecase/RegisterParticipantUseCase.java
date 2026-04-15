package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.event.infrastructure.persistence.jpa.EventCapacityJpaRepository;
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

@AllArgsConstructor
public class RegisterParticipantUseCase extends UseCase<RegisterParticipantCommand, RegistrationResponse> {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventCapacityJpaRepository eventCapacityJpaRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    protected RegistrationResponse internalExecute(RegisterParticipantCommand command) {
        // 1. Verificar duplicado (falla rápido)
        if (registrationRepository.existsByEventIdAndParticipantId(command.eventId(), command.participantId())) {
            throw new DuplicateRegistrationException(command.eventId(), command.participantId());
        }

        // 2. Reservar cupo atómicamente
        int reserved = eventCapacityJpaRepository.reserveIfAvailable(command.eventId());
        if (reserved == 0) {
            Event event = eventRepository.findById(command.eventId())
                    .orElseThrow(() -> new IllegalStateException("Event not found: " + command.eventId()));
            if (event.getStatus() != EventStatus.REGISTRATION_OPEN) {
                throw new RegistrationNotOpenException(command.eventId());
            }
            throw new NoCapacityException(command.eventId());
        }

        // 3. Obtener el evento para determinar precio
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new IllegalStateException("Event not found after capacity reservation: " + command.eventId()));

        Registration registration;

        if (event.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            // GRATUITO: confirmar directamente
            registration = Registration.create(command.eventId(), command.participantId(), RegistrationStatus.CONFIRMED);
            registrationRepository.save(registration);
            domainEventPublisher.publish(new RegistrationConfirmedEvent(
                    registration.getId(),
                    registration.getEventId(),
                    registration.getParticipantId(),
                    registration.getTicketCode(),
                    null
            ));
        } else {
            // PAGO: crear en PENDING_PAYMENT
            registration = Registration.create(command.eventId(), command.participantId(), RegistrationStatus.PENDING_PAYMENT);
            registrationRepository.save(registration);
        }

        return RegistrationResponse.from(registration);
    }
}
