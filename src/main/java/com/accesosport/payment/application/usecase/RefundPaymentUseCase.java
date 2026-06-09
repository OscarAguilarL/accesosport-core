package com.accesosport.payment.application.usecase;

import com.accesosport.payment.domain.events.PaymentRefundedEvent;
import com.accesosport.payment.domain.exception.OxxoRefundNotSupportedException;
import com.accesosport.payment.domain.exception.PaymentNotFoundException;
import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.registration.domain.model.PaymentMethod;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class RefundPaymentUseCase extends UseCase<RefundPaymentUseCase.Command, Void> {

    public record Command(UUID registrationId) {}

    private final PaymentRepository paymentRepository;
    private final RegistrationRepository registrationRepository;
    private final PaymentProcessorPort paymentProcessorPort;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    protected Void internalExecute(Command command) {
        Payment payment = paymentRepository.findByRegistrationId(command.registrationId())
                .orElseThrow(() -> new PaymentNotFoundException(command.registrationId().toString()));

        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed payments can be refunded");
        }

        if (payment.getPaymentMethod() == PaymentMethod.OXXO) {
            throw new OxxoRefundNotSupportedException();
        }

        PaymentProcessorPort.RefundResult refundResult = paymentProcessorPort.refund(payment.getStripePaymentIntentId());
        payment.refund(refundResult.refundId());
        paymentRepository.save(payment);

        Registration registration = registrationRepository.findById(command.registrationId())
                .orElseThrow(() -> new IllegalStateException("Registration not found"));
        registration.cancel();
        registrationRepository.save(registration);

        domainEventPublisher.publish(new PaymentRefundedEvent(payment.getId(), payment.getRegistrationId()));

        return null;
    }
}
