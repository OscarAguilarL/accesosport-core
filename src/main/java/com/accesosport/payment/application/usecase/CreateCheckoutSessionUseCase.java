package com.accesosport.payment.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.payment.application.dto.CheckoutSessionResponse;
import com.accesosport.payment.application.service.RegistrationPaymentAccessService;
import com.accesosport.payment.domain.exception.OrganizerStripeNotLinkedException;
import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.domain.model.OrganizerFeeCalculator;
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

    public record Command(
            UUID registrationId,
            UUID authenticatedUserId,
            String anonymousAccessToken
    ) {}

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventModalityRepository eventModalityRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessorPort paymentProcessorPort;
    private final RegistrationPaymentAccessService accessService;

    @Override
    protected CheckoutSessionResponse internalExecute(Command command) {
        Registration registration = registrationRepository.findByIdForUpdate(command.registrationId())
                .orElseThrow(() -> new IllegalArgumentException("Registration not found: " + command.registrationId()));

        accessService.assertCanAccess(registration, command.authenticatedUserId(), command.anonymousAccessToken());

        if (registration.getStatus() != RegistrationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Registration is not awaiting payment");
        }

        var existingPayment = paymentRepository.findByRegistrationId(command.registrationId());
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();

            if (payment.getStatus() == PaymentStatus.CONFIRMED) {
                return new CheckoutSessionResponse(payment.getStripeSessionId(), null, true);
            }

            PaymentProcessorPort.CheckoutSessionInfo sessionInfo =
                    paymentProcessorPort.retrieveCheckoutSession(payment.getStripeSessionId());

            if ("complete".equals(sessionInfo.status())) {
                return new CheckoutSessionResponse(payment.getStripeSessionId(), null, true);
            }

            if ("open".equals(sessionInfo.status())) {
                return new CheckoutSessionResponse(payment.getStripeSessionId(), sessionInfo.url(), false);
            }

            // Session expired → create new one
            payment.incrementCheckoutAttempt();
            String idempotencyKey = "checkout:" + command.registrationId() + ":" + payment.getCheckoutAttempt();

            Event event = eventRepository.findById(registration.getEventId())
                    .orElseThrow(() -> new IllegalArgumentException("Event not found"));
            EventModality modality = eventModalityRepository.findById(registration.getModalityId())
                    .orElseThrow(() -> new IllegalArgumentException("Modality not found"));
            BigDecimal basePrice = resolveBasePrice(registration, modality);
            BigDecimal serviceFee = ServiceFeeCalculator.calculate(basePrice);
            BigDecimal organizerFee = OrganizerFeeCalculator.calculate(basePrice);
            UserOrganizerProfile organizer = loadOrganizer(event);

            long amountTotalCentavos = toCentavos(basePrice.add(serviceFee));
            long serviceFeeCentavos = toCentavos(serviceFee);
            long organizerFeeCentavos = toCentavos(organizerFee);

            PaymentProcessorPort.CheckoutSessionResult result = paymentProcessorPort.createCheckoutSession(
                    new PaymentProcessorPort.CreateCheckoutSessionCommand(
                            command.registrationId(),
                            payment.getId(),
                            registration.getEventId(),
                            event.getName(),
                            amountTotalCentavos,
                            serviceFeeCentavos,
                            organizer.getStripeAccountId(),
                            idempotencyKey,
                            organizerFeeCentavos
                    )
            );

            payment.updateStripeSessionId(result.sessionId());
            paymentRepository.save(payment);
            return new CheckoutSessionResponse(result.sessionId(), result.checkoutUrl(), false);
        }

        // No payment yet → create first session
        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        EventModality modality = eventModalityRepository.findById(registration.getModalityId())
                .orElseThrow(() -> new IllegalArgumentException("Modality not found"));
        BigDecimal basePrice = resolveBasePrice(registration, modality);
        BigDecimal serviceFee = ServiceFeeCalculator.calculate(basePrice);
        BigDecimal organizerFee = OrganizerFeeCalculator.calculate(basePrice);
        UserOrganizerProfile organizer = loadOrganizer(event);

        long amountTotalCentavos = toCentavos(basePrice.add(serviceFee));
        long serviceFeeCentavos = toCentavos(serviceFee);
        long organizerFeeCentavos = toCentavos(organizerFee);

        String idempotencyKey = "checkout:" + command.registrationId() + ":1";
        UUID newPaymentId = UUID.randomUUID();

        PaymentProcessorPort.CheckoutSessionResult result = paymentProcessorPort.createCheckoutSession(
                new PaymentProcessorPort.CreateCheckoutSessionCommand(
                        command.registrationId(),
                        newPaymentId,
                        registration.getEventId(),
                        event.getName(),
                        amountTotalCentavos,
                        serviceFeeCentavos,
                        organizer.getStripeAccountId(),
                        idempotencyKey,
                        organizerFeeCentavos
                )
        );

        Payment payment = Payment.create(newPaymentId, command.registrationId(), result.sessionId(), basePrice, serviceFee);
        paymentRepository.save(payment);

        return new CheckoutSessionResponse(result.sessionId(), result.checkoutUrl(), false);
    }

    private BigDecimal resolveBasePrice(Registration registration, EventModality modality) {
        return (!registration.isWantsShirt() && modality.getPriceWithoutShirt() != null)
                ? modality.getPriceWithoutShirt()
                : modality.getPrice();
    }

    private UserOrganizerProfile loadOrganizer(Event event) {
        UserOrganizerProfile organizer = organizerProfileRepository
                .findByUserId(event.getCreatedBy().getId())
                .orElseThrow(() -> new IllegalArgumentException("Organizer profile not found"));
        if (!organizer.isStripeLinked()) {
            throw new OrganizerStripeNotLinkedException();
        }
        return organizer;
    }

    private long toCentavos(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
