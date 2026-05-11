package com.accesosport.registration.application.service;

import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.registration.application.dto.CancelRegistrationCommand;
import com.accesosport.registration.application.usecase.CancelRegistrationUseCase;
import com.accesosport.registration.domain.events.PendingPaymentExpiredEvent;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationCleanupService {

    private final RegistrationRepository registrationRepository;
    private final EventModalityRepository eventModalityRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Value("${app.registration.cleanup.card-expiry-minutes:30}")
    private int cardExpiryMinutes;

    @Value("${app.registration.cleanup.cash-expiry-days:3}")
    private int cashExpiryDays;

    public void cleanupExpiredPendingPayments() {
        LocalDateTime cardThreshold = LocalDateTime.now().minusMinutes(cardExpiryMinutes);
        LocalDateTime cashThreshold = LocalDateTime.now().minusDays(cashExpiryDays);

        List<Registration> expired = registrationRepository.findExpiredPendingPayments(cardThreshold, cashThreshold);

        if (expired.isEmpty()) {
            return;
        }

        log.info("[Cleanup] Found {} expired PENDING_PAYMENT registrations", expired.size());

        CancelRegistrationUseCase cancelRegistrationUseCase =
                new CancelRegistrationUseCase(registrationRepository, eventModalityRepository, domainEventPublisher);

        expired.forEach(registration -> {
            try {
                cancelRegistrationUseCase.execute(new CancelRegistrationCommand(
                        registration.getId(),
                        null,   // system cancellation — no requesterId
                        true    // isAdmin = true para bypass de ownership
                ));

                if (registration.getPaymentMethod() != null
                        && registration.getPaymentMethod().requiresExtendedExpiry()) {
                    domainEventPublisher.publish(new PendingPaymentExpiredEvent(
                            registration.getId(),
                            registration.getEventId(),
                            registration.getParticipantId(),
                            registration.getPaymentMethod()
                    ));
                    log.info("[Cleanup] Published PendingPaymentExpiredEvent for registration {}", registration.getId());
                }
            } catch (Exception e) {
                log.error("[Cleanup] Failed to cancel expired registration {}", registration.getId(), e);
            }
        });
    }
}
