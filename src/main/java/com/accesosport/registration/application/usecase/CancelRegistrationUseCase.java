package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.repository.EventModalityRepository;
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
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class CancelRegistrationUseCase extends UseCase<CancelRegistrationCommand, RegistrationResponse> {

    private final RegistrationRepository registrationRepository;
    private final EventModalityRepository eventModalityRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    protected RegistrationResponse internalExecute(CancelRegistrationCommand command) {
        Registration registration = registrationRepository.findById(command.registrationId())
                .orElseThrow(() -> new RegistrationNotFoundException(command.registrationId()));

        if (!command.isAdmin() && !registration.getParticipantId().equals(command.requesterId())) {
            throw new RegistrationAccessDeniedException(command.registrationId(), command.requesterId());
        }

        registration.cancel();
        registrationRepository.save(registration);

        if (registration.getModalityId() != null) {
            eventModalityRepository.release(registration.getModalityId());
        }

        domainEventPublisher.publish(new RegistrationCancelledEvent(
                registration.getId(),
                registration.getEventId(),
                registration.getParticipantId()
        ));

        return RegistrationResponse.from(registration);
    }
}
