package com.accesosport.registration.infrastructure.listeners;

import com.accesosport.event.domain.events.RegistrationClosedEvent;
import com.accesosport.registration.application.dto.AssignBibNumbersCommand;
import com.accesosport.registration.application.usecase.AssignBibNumbersUseCase;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BibNumberAssignmentEventHandler {

    private final RegistrationRepository registrationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("domainEventExecutor")
    public void handle(RegistrationClosedEvent event) {
        try {
            log.info("[BibNumbers] Assigning bib numbers for event {}", event.getEventId());
            AssignBibNumbersUseCase useCase = new AssignBibNumbersUseCase(registrationRepository);
            useCase.execute(new AssignBibNumbersCommand(event.getEventId()));
            log.info("[BibNumbers] Bib numbers assigned successfully for event {}", event.getEventId());
        } catch (Exception e) {
            log.error("[BibNumbers] Failed to assign bib numbers for event {}", event.getEventId(), e);
            // NO relanzar — la transacción principal ya committed
        }
    }
}
