package com.accesosport.payment.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class OrganizerFeeCalculator {

    private static final BigDecimal MIN_FEE = new BigDecimal("10.00");
    private static final BigDecimal FEE_RATE = new BigDecimal("0.05"); // breakpoint: $200 (5% × $200 = $10)

    private OrganizerFeeCalculator() {}

    public static BigDecimal calculate(BigDecimal basePrice) {
        BigDecimal percentFee = basePrice.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        return MIN_FEE.max(percentFee);
    }
}
