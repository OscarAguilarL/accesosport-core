package com.accesosport.payment.application.usecase;

import com.accesosport.payment.domain.events.PaymentRefundedEvent;
import com.accesosport.payment.domain.events.PaymentManualRefundRequiredEvent;
import com.accesosport.payment.domain.exception.PaymentNotFoundException;
import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.registration.domain.model.PaymentMethod;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class RefundPaymentUseCase extends UseCase<RefundPaymentUseCase.Command, Void> {

    public record Command(UUID registrationId, boolean partial) {}

    private final PaymentRepository paymentRepository;
    private final PaymentProcessorPort paymentProcessorPort;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    protected Void internalExecute(Command command) {
        Payment payment = paymentRepository.findByRegistrationId(command.registrationId())
                .orElseThrow(() -> new PaymentNotFoundException(command.registrationId().toString()));

        if (payment.getStatus() != PaymentStatus.CONFIRMED
                && payment.getStatus() != PaymentStatus.REFUND_FAILED) {
            throw new IllegalStateException("Only confirmed or failed-refund payments can be refunded");
        }

        if (payment.getPaymentMethod() == PaymentMethod.OXXO) {
            payment.initiateManualRefund();
            paymentRepository.save(payment);
            domainEventPublisher.publish(new PaymentManualRefundRequiredEvent(
                    payment.getId(), payment.getRegistrationId()));
            return null;
        }

        payment.initiateRefund();
        paymentRepository.save(payment);

        try {
            PaymentProcessorPort.RefundResult refundResult;
            if (command.partial()) {
                long amountCentavos = payment.getAmountTotal()
                        .multiply(new BigDecimal("0.92"))
                        .setScale(0, RoundingMode.DOWN)
                        .multiply(new BigDecimal("100"))
                        .longValue();
                refundResult = paymentProcessorPort.refundPartial(
                        payment.getStripePaymentIntentId(), payment.getId(), amountCentavos);
            } else {
                refundResult = paymentProcessorPort.refund(
                        payment.getStripePaymentIntentId(), payment.getId());
            }
            payment.completeRefund(refundResult.refundId());
            paymentRepository.save(payment);
            domainEventPublisher.publish(new PaymentRefundedEvent(payment.getId(), payment.getRegistrationId()));
        } catch (Exception e) {
            log.error("Refund failed for payment {}: {}", payment.getId(), e.getMessage());
            payment.failRefund(e.getMessage());
            paymentRepository.save(payment);
        }

        return null;
    }
}
