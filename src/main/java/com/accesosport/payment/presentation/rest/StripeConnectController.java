package com.accesosport.payment.presentation.rest;

import com.accesosport.auth.infrastructure.security.CustomUserDetails;
import com.accesosport.payment.application.dto.ConnectOnboardingResponse;
import com.accesosport.payment.application.dto.ConnectStatusResponse;
import com.accesosport.payment.application.service.PaymentApplicationService;
import com.accesosport.payment.domain.exception.InvalidWebhookSignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StripeConnectController {

    private final PaymentApplicationService paymentApplicationService;

    @PostMapping("/api/v1/stripe/connect/onboard")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<ConnectOnboardingResponse> startOnboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ConnectOnboardingResponse response = paymentApplicationService.initiateConnectOnboarding(
                userDetails.getUserId()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/stripe/connect/status")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<ConnectStatusResponse> getConnectStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(paymentApplicationService.getConnectStatus(userDetails.getUserId()));
    }

    @PostMapping("/api/v1/webhooks/stripe/connect")
    public ResponseEntity<Void> handleConnectWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        try {
            paymentApplicationService.handleConnectWebhookEvent(payload, signature);
        } catch (InvalidWebhookSignatureException e) {
            throw e; // propagates to @ExceptionHandler → 400
        } catch (Exception e) {
            log.error("Error dispatching Stripe Connect webhook event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}
