package com.accesosport.payment.application.service;

import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.payment.application.dto.CheckoutSessionResponse;
import com.accesosport.payment.application.dto.ConnectOnboardingResponse;
import com.accesosport.payment.application.dto.ConnectStatusResponse;
import com.accesosport.payment.application.dto.PaymentStatusResponse;
import com.accesosport.payment.application.usecase.ConfirmPaymentUseCase;
import com.accesosport.payment.application.usecase.ConnectOnboardingUseCase;
import com.accesosport.payment.application.usecase.CreateCheckoutSessionUseCase;
import com.accesosport.payment.application.usecase.RefundPaymentUseCase;
import com.accesosport.payment.domain.events.PaymentFailedEvent;
import com.accesosport.payment.domain.exception.PaymentNotFoundException;
import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventModalityRepository eventModalityRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final PaymentProcessorPort paymentProcessorPort;
    private final DomainEventPublisher domainEventPublisher;

    @Value("${stripe.webhook-secret}")
    private String checkoutWebhookSecret;

    @Value("${stripe.connect-webhook-secret}")
    private String connectWebhookSecret;

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(UUID registrationId, String successUrl, String cancelUrl) {
        return new CreateCheckoutSessionUseCase(
                registrationRepository, eventRepository, eventModalityRepository,
                organizerProfileRepository, paymentRepository, paymentProcessorPort
        ).execute(new CreateCheckoutSessionUseCase.Command(registrationId, successUrl, cancelUrl));
    }

    @Transactional
    public void handleCheckoutWebhookEvent(String payload, String signature) {
        PaymentProcessorPort.StripeWebhookEvent event = paymentProcessorPort.parseAndValidateWebhookEvent(
                payload, signature, checkoutWebhookSecret);
        switch (event.type()) {
            case "checkout.session.completed" -> {
                if ("paid".equals(event.paymentStatus())) {
                    confirmPayment(event.sessionId(), event.paymentIntentId(), event.paymentMethodType());
                }
            }
            case "checkout.session.async_payment_succeeded" ->
                    confirmPayment(event.sessionId(), event.paymentIntentId(), event.paymentMethodType());
            case "checkout.session.async_payment_failed" ->
                    failPayment(event.sessionId());
            default -> log.debug("Unhandled Stripe checkout event: {}", event.type());
        }
    }

    @Transactional
    public void handleConnectWebhookEvent(String payload, String signature) {
        PaymentProcessorPort.StripeWebhookEvent event = paymentProcessorPort.parseAndValidateWebhookEvent(
                payload, signature, connectWebhookSecret);
        if ("account.updated".equals(event.type()) && event.connectedAccountId() != null) {
            handleAccountUpdated(event.connectedAccountId());
        }
    }

    @Transactional
    public void refundPayment(UUID registrationId) {
        new RefundPaymentUseCase(paymentRepository, registrationRepository, paymentProcessorPort, domainEventPublisher)
                .execute(new RefundPaymentUseCase.Command(registrationId));
    }

    @Transactional
    public ConnectOnboardingResponse initiateConnectOnboarding(UUID organizerUserId, String returnUrl, String refreshUrl) {
        return new ConnectOnboardingUseCase(organizerProfileRepository, paymentProcessorPort)
                .execute(new ConnectOnboardingUseCase.Command(organizerUserId, returnUrl, refreshUrl));
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(UUID registrationId) {
        Payment payment = paymentRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new PaymentNotFoundException(registrationId.toString()));

        return new PaymentStatusResponse(
                payment.getStatus().name(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null,
                payment.getAmountTotal(),
                payment.getBaseAmount(),
                payment.getServiceFeeAmount()
        );
    }

    @Transactional
    public ConnectStatusResponse getConnectStatus(UUID organizerUserId) {
        return organizerProfileRepository.findByUserId(organizerUserId)
                .map(profile -> {
                    boolean chargesEnabled = false;
                    if (profile.getStripeAccountId() != null) {
                        try {
                            PaymentProcessorPort.ConnectAccountStatus stripeStatus =
                                    paymentProcessorPort.getConnectAccountStatus(profile.getStripeAccountId());
                            chargesEnabled = stripeStatus.chargesEnabled();
                            if (stripeStatus.detailsSubmitted() && !profile.isStripeOnboardingCompleted()) {
                                profile.completeOnboarding();
                                organizerProfileRepository.save(profile);
                            }
                        } catch (Exception e) {
                            log.warn("Could not fetch Stripe account status for organizer {}", organizerUserId, e);
                        }
                    }
                    return new ConnectStatusResponse(
                            profile.getStripeAccountId(),
                            profile.isStripeOnboardingCompleted(),
                            chargesEnabled
                    );
                })
                .orElse(new ConnectStatusResponse(null, false, false));
    }

    private void confirmPayment(String sessionId, String paymentIntentId, String paymentMethodType) {
        new ConfirmPaymentUseCase(paymentRepository, registrationRepository, domainEventPublisher)
                .execute(new ConfirmPaymentUseCase.Command(sessionId, paymentIntentId, paymentMethodType));
    }

    private void failPayment(String sessionId) {
        paymentRepository.findByStripeSessionId(sessionId).ifPresent(payment -> {
            payment.fail();
            paymentRepository.save(payment);

            registrationRepository.findById(payment.getRegistrationId()).ifPresent(registration -> {
                registration.cancel();
                registrationRepository.save(registration);
            });

            domainEventPublisher.publish(new PaymentFailedEvent(payment.getRegistrationId()));
        });
    }

    private void handleAccountUpdated(String stripeAccountId) {
        PaymentProcessorPort.ConnectAccountStatus status = paymentProcessorPort.getConnectAccountStatus(stripeAccountId);
        if (!status.detailsSubmitted()) {
            return;
        }
        organizerProfileRepository.findByStripeAccountId(stripeAccountId).ifPresent(profile -> {
            if (!profile.isStripeOnboardingCompleted()) {
                profile.completeOnboarding();
                organizerProfileRepository.save(profile);
            }
        });
    }
}
