package com.accesosport.payment.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ServiceFeeCalculator {

    private static final BigDecimal MIN_FEE = new BigDecimal("20.00");
    private static final BigDecimal FEE_RATE = new BigDecimal("0.10"); // breakpoint: $200 (10% × $200 = $20)

    private ServiceFeeCalculator() {}

    public static BigDecimal calculate(BigDecimal basePrice) {
        BigDecimal percentFee = basePrice.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        return MIN_FEE.max(percentFee);
    }
}
