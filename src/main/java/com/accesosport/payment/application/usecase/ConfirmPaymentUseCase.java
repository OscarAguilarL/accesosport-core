package com.accesosport.payment.application.usecase;

import com.accesosport.payment.domain.events.PaymentConfirmedEvent;
import com.accesosport.payment.domain.exception.PaymentNotFoundException;
import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.registration.domain.events.RegistrationConfirmedEvent;
import com.accesosport.registration.domain.model.PaymentMethod;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConfirmPaymentUseCase extends UseCase<ConfirmPaymentUseCase.Command, Void> {

    public record Command(String stripeSessionId, String paymentIntentId, String paymentMethodType) {}

    private final PaymentRepository paymentRepository;
    private final RegistrationRepository registrationRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final PaymentProcessorPort paymentProcessorPort;

    @Override
    protected Void internalExecute(Command command) {
        Payment payment = paymentRepository.findByStripeSessionId(command.stripeSessionId())
                .orElseThrow(() -> new PaymentNotFoundException(command.stripeSessionId()));

        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            return null;
        }

        String resolvedMethodType = command.paymentMethodType();
        if (resolvedMethodType == null && command.paymentIntentId() != null) {
            resolvedMethodType = paymentProcessorPort.getActualPaymentMethod(command.paymentIntentId());
        }

        PaymentMethod method = resolvePaymentMethod(resolvedMethodType);
        payment.confirm(command.paymentIntentId(), method);
        paymentRepository.save(payment);

        Registration registration = registrationRepository.findById(payment.getRegistrationId())
                .orElseThrow(() -> new IllegalStateException("Registration not found for payment " + payment.getId()));

        registration.confirm(method);
        registrationRepository.save(registration);

        domainEventPublisher.publish(new RegistrationConfirmedEvent(
                registration.getId(),
                registration.getEventId(),
                registration.getParticipantId(),
                registration.getTicketCode(),
                registration.getBibNumber()
        ));
        domainEventPublisher.publish(new PaymentConfirmedEvent(payment.getId(), payment.getRegistrationId()));

        return null;
    }

    private PaymentMethod resolvePaymentMethod(String stripeType) {
        if (stripeType == null) return PaymentMethod.OTHER;
        return switch (stripeType.toLowerCase()) {
            case "oxxo" -> PaymentMethod.OXXO;
            case "card" -> PaymentMethod.CARD;
            default -> PaymentMethod.OTHER;
        };
    }
}
