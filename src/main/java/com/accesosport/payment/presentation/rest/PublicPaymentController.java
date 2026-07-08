package com.accesosport.payment.presentation.rest;

import com.accesosport.payment.application.dto.PricingBreakdownResponse;
import com.accesosport.payment.application.service.PaymentApplicationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/public/payments")
@Validated
@RequiredArgsConstructor
public class PublicPaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @GetMapping("/fee-breakdown")
    public ResponseEntity<PricingBreakdownResponse> getFeeBreakdown(
            @RequestParam
            @Positive
            BigDecimal basePrice
    ) {
        return ResponseEntity.ok(paymentApplicationService.calculatePricingBreakdown(basePrice));
    }
}
