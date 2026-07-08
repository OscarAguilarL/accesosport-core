package com.accesosport.payment.infrastructure.listeners;

import com.accesosport.event.domain.events.EventCancelledEvent;
import com.accesosport.payment.application.service.PaymentApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCancellationRefundHandler {

    private final PaymentApplicationService paymentApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("domainEventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(EventCancelledEvent event) {
        for (var registrationId : event.getAffectedRegistrationIds()) {
            try {
                paymentApplicationService.refundPayment(registrationId);
            } catch (Exception e) {
                log.error("Failed to process refund for registration {} during event {} cancellation",
                        registrationId, event.getEventId(), e);
            }
        }
    }
}
