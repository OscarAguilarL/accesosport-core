package com.accesosport.payment.presentation.rest;

import com.accesosport.auth.infrastructure.security.CustomUserDetails;
import com.accesosport.payment.application.dto.CheckoutSessionResponse;
import com.accesosport.payment.application.dto.PaymentStatusResponse;
import com.accesosport.payment.application.service.PaymentApplicationService;
import com.accesosport.payment.domain.exception.InvalidWebhookSignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    public record CreateCheckoutSessionRequest(UUID registrationId) {}

    @PostMapping("/api/v1/payments/checkout-session")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @RequestBody CreateCheckoutSessionRequest body,
            @RequestHeader(value = "X-Registration-Access-Token", required = false) String accessToken,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails != null ? userDetails.getUserId() : null;
        CheckoutSessionResponse response = paymentApplicationService.createCheckoutSession(
                body.registrationId(), userId, accessToken
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/payments/registration/{registrationId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable UUID registrationId,
            @RequestHeader(value = "X-Registration-Access-Token", required = false) String accessToken,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails != null ? userDetails.getUserId() : null;
        return ResponseEntity.ok(paymentApplicationService.getPaymentStatus(registrationId, userId, accessToken));
    }

    @PostMapping("/api/v1/webhooks/stripe/checkout")
    public ResponseEntity<Void> handleCheckoutWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        try {
            paymentApplicationService.handleCheckoutWebhookEvent(payload, signature);
        } catch (InvalidWebhookSignatureException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error dispatching Stripe checkout webhook event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/admin/payments/{paymentId}/complete-manual-refund")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> completeManualRefund(@PathVariable UUID paymentId) {
        paymentApplicationService.completeManualRefund(paymentId);
        return ResponseEntity.ok().build();
    }
}
