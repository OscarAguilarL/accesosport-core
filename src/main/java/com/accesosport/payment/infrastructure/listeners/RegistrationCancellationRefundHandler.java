package com.accesosport.payment.infrastructure.listeners;

import com.accesosport.payment.application.service.PaymentApplicationService;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.registration.domain.events.RegistrationCancelledEvent;
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
public class RegistrationCancellationRefundHandler {

    private final PaymentRepository paymentRepository;
    private final PaymentApplicationService paymentApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("domainEventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(RegistrationCancelledEvent event) {
        try {
            if (event.getDaysUntilEvent() <= 15) {
                return;
            }
            paymentRepository.findByRegistrationId(event.getRegistrationId())
                    .filter(p -> p.getStatus() == PaymentStatus.CONFIRMED)
                    .ifPresent(p -> paymentApplicationService.refundPaymentPartial(event.getRegistrationId()));
        } catch (Exception e) {
            log.error("Failed to process refund for cancelled registration {}", event.getRegistrationId(), e);
        }
    }
}
