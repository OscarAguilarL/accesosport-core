package com.accesosport.registration.application.usecase;

import com.accesosport.event.infrastructure.persistence.jpa.EventCapacityJpaRepository;
import com.accesosport.registration.application.dto.CancelRegistrationCommand;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.domain.events.RegistrationCancelledEvent;
import com.accesosport.registration.domain.exception.RegistrationAccessDeniedException;
import com.accesosport.registration.domain.exception.RegistrationNotFoundException;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CancelRegistrationUseCase extends UseCase<CancelRegistrationCommand, RegistrationResponse> {

    private final RegistrationRepository registrationRepository;
    private final EventCapacityJpaRepository eventCapacityJpaRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    protected RegistrationResponse internalExecute(CancelRegistrationCommand command) {
        Registration registration = registrationRepository.findById(command.registrationId())
                .orElseThrow(() -> new RegistrationNotFoundException(command.registrationId()));

        // Verificar ownership (si no es admin, solo puede cancelar las propias)
        if (!command.isAdmin() && !registration.getParticipantId().equals(command.requesterId())) {
            throw new RegistrationAccessDeniedException(command.registrationId(), command.requesterId());
        }

        registration.cancel(); // lanza IllegalStateException si ya está CANCELLED
        registrationRepository.save(registration);

        // Liberar cupo
        eventCapacityJpaRepository.release(registration.getEventId());

        domainEventPublisher.publish(new RegistrationCancelledEvent(
                registration.getId(),
                registration.getEventId(),
                registration.getParticipantId()
        ));

        return RegistrationResponse.from(registration);
    }
}
