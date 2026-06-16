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
import com.accesosport.payment.domain.events.PaymentRefundedEvent;
import com.accesosport.payment.domain.exception.PaymentNotFoundException;
import com.accesosport.payment.domain.model.Payment;
import com.accesosport.payment.domain.model.PaymentStatus;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.accesosport.payment.domain.port.PaymentRepository;
import com.accesosport.payment.domain.port.StripeWebhookEventRepository;
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
    private final StripeWebhookEventRepository webhookEventRepository;

    @Value("${stripe.webhook-secret}")
    private String checkoutWebhookSecret;

    @Value("${stripe.connect-webhook-secret}")
    private String connectWebhookSecret;

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(UUID registrationId, UUID authenticatedUserId, String anonymousAccessToken) {
        return new CreateCheckoutSessionUseCase(
                registrationRepository, eventRepository, eventModalityRepository,
                organizerProfileRepository, paymentRepository, paymentProcessorPort,
                new RegistrationPaymentAccessService()
        ).execute(new CreateCheckoutSessionUseCase.Command(registrationId, authenticatedUserId, anonymousAccessToken));
    }

    @Transactional
    public void handleCheckoutWebhookEvent(String payload, String signature) {
        PaymentProcessorPort.StripeWebhookEvent event = paymentProcessorPort.parseAndValidateCheckoutWebhookEvent(
                payload, signature, checkoutWebhookSecret);
        processWithIdempotency(event.eventId(), event.type(), () -> {
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
        });
    }

    @Transactional
    public void handleConnectWebhookEvent(String payload, String signature) {
        PaymentProcessorPort.StripeWebhookEvent event = paymentProcessorPort.parseAndValidateConnectWebhookEvent(
                payload, signature, connectWebhookSecret);
        if ("account.updated".equals(event.type()) && event.connectedAccountId() != null) {
            processWithIdempotency(event.eventId(), event.type(),
                    () -> handleAccountUpdated(event.connectedAccountId()));
        }
    }

    @Transactional
    public void refundPayment(UUID registrationId) {
        new RefundPaymentUseCase(paymentRepository, paymentProcessorPort, domainEventPublisher)
                .execute(new RefundPaymentUseCase.Command(registrationId));
    }

    @Transactional
    public void completeManualRefund(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId.toString()));
        payment.completeManualRefund();
        paymentRepository.save(payment);
        domainEventPublisher.publish(new PaymentRefundedEvent(payment.getId(), payment.getRegistrationId()));
    }

    @Transactional
    public ConnectOnboardingResponse initiateConnectOnboarding(UUID organizerUserId) {
        return new ConnectOnboardingUseCase(organizerProfileRepository, paymentProcessorPort)
                .execute(new ConnectOnboardingUseCase.Command(organizerUserId));
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(UUID registrationId, UUID authenticatedUserId, String anonymousAccessToken) {
        Payment payment = paymentRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new PaymentNotFoundException(registrationId.toString()));

        registrationRepository.findById(registrationId).ifPresent(reg ->
                new RegistrationPaymentAccessService().assertCanAccess(reg, authenticatedUserId, anonymousAccessToken)
        );

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
                    boolean onboardingCompleted = profile.isStripeOnboardingCompleted();
                    boolean transfersActive = profile.isStripeTransfersActive();
                    boolean payoutsActive = false;
                    boolean hasRequirements = false;
                    boolean underStripeReview = false;
                    boolean stripeStatusFetched = false;

                    if (profile.getStripeAccountId() != null) {
                        try {
                            PaymentProcessorPort.ConnectAccountStatus stripeStatus =
                                    paymentProcessorPort.getConnectAccountStatus(profile.getStripeAccountId());
                            stripeStatusFetched = true;
                            transfersActive = stripeStatus.transfersActive();
                            payoutsActive = stripeStatus.payoutsActive();
                            hasRequirements = stripeStatus.hasUserRequirements();
                            underStripeReview = stripeStatus.underStripeReview();
                            onboardingCompleted = stripeStatus.onboardingSubmitted();

                            if (isReady(stripeStatus) && !profile.isStripeTransfersActive()) {
                                profile.activateStripeTransfers();
                                profile.completeOnboarding();
                                organizerProfileRepository.save(profile);
                            } else if (!isReady(stripeStatus) && profile.isStripeTransfersActive()) {
                                profile.deactivateStripeTransfers();
                                organizerProfileRepository.save(profile);
                            }
                        } catch (Exception e) {
                            log.warn("Could not fetch Stripe account status for organizer {}", organizerUserId, e);
                        }
                    }
                    if (!stripeStatusFetched && profile.isStripeTransfersActive()) {
                        transfersActive = true;
                        payoutsActive = true;
                    }
                    return new ConnectStatusResponse(
                            profile.getStripeAccountId(),
                            onboardingCompleted,
                            transfersActive,
                            payoutsActive,
                            hasRequirements,
                            resolveConnectState(
                                    profile.getStripeAccountId(), onboardingCompleted, transfersActive,
                                    payoutsActive, hasRequirements, underStripeReview)
                    );
                })
                .orElse(new ConnectStatusResponse(
                        null, false, false, false, false, "NOT_CONNECTED"));
    }

    private void confirmPayment(String sessionId, String paymentIntentId, String paymentMethodType) {
        new ConfirmPaymentUseCase(paymentRepository, registrationRepository, domainEventPublisher, paymentProcessorPort)
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
        organizerProfileRepository.findByStripeAccountId(stripeAccountId).ifPresent(profile -> {
            if (status.onboardingSubmitted() && !profile.isStripeOnboardingCompleted()) {
                profile.completeOnboarding();
            }
            if (isReady(status)) {
                profile.activateStripeTransfers();
            } else {
                profile.deactivateStripeTransfers();
            }
            organizerProfileRepository.save(profile);
        });
    }

    private boolean isReady(PaymentProcessorPort.ConnectAccountStatus status) {
        return status.transfersActive()
                && status.payoutsActive()
                && !status.hasUserRequirements();
    }

    private String resolveConnectState(
            String accountId,
            boolean onboardingCompleted,
            boolean transfersActive,
            boolean payoutsActive,
            boolean hasUserRequirements,
            boolean underStripeReview
    ) {
        if (accountId == null) return "NOT_CONNECTED";
        if (hasUserRequirements || !onboardingCompleted) return "ONBOARDING_REQUIRED";
        if (underStripeReview) return "STRIPE_REVIEW";
        if (!transfersActive || !payoutsActive) return "PAYOUTS_RESTRICTED";
        return "READY";
    }

    private void processWithIdempotency(String eventId, String eventType, Runnable handler) {
        if (eventId == null) {
            handler.run();
            return;
        }
        int claimed = webhookEventRepository.claimAsProcessing(eventId, eventType);
        if (claimed == 0) {
            log.debug("Skipping duplicate webhook event: {}", eventId);
            return;
        }
        try {
            handler.run();
            webhookEventRepository.markProcessed(eventId);
        } catch (Exception e) {
            webhookEventRepository.markFailed(eventId, e.getMessage());
            throw e;
        }
    }
}
