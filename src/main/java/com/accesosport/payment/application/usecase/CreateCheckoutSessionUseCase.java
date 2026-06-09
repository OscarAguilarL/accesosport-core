package com.accesosport.payment.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.payment.application.dto.CheckoutSessionResponse;
import com.accesosport.payment.domain.exception.OrganizerStripeNotLinkedException;
import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.ServiceFeeCalculator;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.model.RegistrationStatus;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@AllArgsConstructor
public class CreateCheckoutSessionUseCase extends UseCase<CreateCheckoutSessionUseCase.Command, CheckoutSessionResponse> {

    public record Command(UUID registrationId, String successUrl, String cancelUrl) {}

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventModalityRepository eventModalityRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessorPort paymentProcessorPort;

    @Override
    protected CheckoutSessionResponse internalExecute(Command command) {
        Registration registration = registrationRepository.findById(command.registrationId())
                .orElseThrow(() -> new IllegalArgumentException("Registration not found: " + command.registrationId()));

        if (registration.getStatus() != RegistrationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Registration is not awaiting payment");
        }

        if (paymentRepository.findByRegistrationId(command.registrationId()).isPresent()) {
            PaymentProcessorPort.CheckoutSessionResult existing = null;
            var existingPayment = paymentRepository.findByRegistrationId(command.registrationId()).get();
            return new CheckoutSessionResponse(existingPayment.getStripeSessionId(), null);
        }

        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        EventModality modality = eventModalityRepository.findById(registration.getModalityId())
                .orElseThrow(() -> new IllegalArgumentException("Modality not found"));

        BigDecimal basePrice = (!registration.isWantsShirt() && modality.getPriceWithoutShirt() != null)
                ? modality.getPriceWithoutShirt()
                : modality.getPrice();

        BigDecimal serviceFee = ServiceFeeCalculator.calculate(basePrice);
        BigDecimal amountTotal = basePrice.add(serviceFee);

        UserOrganizerProfile organizer = organizerProfileRepository
                .findByUserId(event.getCreatedBy().getId())
                .orElseThrow(() -> new IllegalArgumentException("Organizer profile not found"));

        if (!organizer.isStripeLinked()) {
            throw new OrganizerStripeNotLinkedException();
        }

        long amountTotalCentavos = amountTotal.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
        long serviceFeeCentavos = serviceFee.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();

        PaymentProcessorPort.CheckoutSessionResult result = paymentProcessorPort.createCheckoutSession(
                new PaymentProcessorPort.CreateCheckoutSessionCommand(
                        command.registrationId(),
                        event.getName(),
                        amountTotalCentavos,
                        serviceFeeCentavos,
                        organizer.getStripeAccountId(),
                        command.successUrl(),
                        command.cancelUrl()
                )
        );

        Payment payment = Payment.create(command.registrationId(), result.sessionId(), basePrice, serviceFee);
        paymentRepository.save(payment);

        return new CheckoutSessionResponse(result.sessionId(), result.checkoutUrl());
    }
}
