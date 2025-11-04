package com.grupocaos.products.athletix.event.domain.model;


import java.math.BigDecimal;

public record Distance(BigDecimal value, DistanceUnit unit) {

    public static Distance of(BigDecimal value, DistanceUnit unit) {
        validate(value, unit);
        return new Distance(value, unit);
    }

    private static void validate(BigDecimal value, DistanceUnit unit) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Distance value must be greater than zero");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Distance unit must not be null");
        }
    }

    public BigDecimal convertToKilometers() {
        if (unit == DistanceUnit.MI) {
            return value.multiply(BigDecimal.valueOf(1.60934));
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Distance distance = (Distance) obj;
        return value.compareTo(distance.value) == 0 && unit == distance.unit;
    }

    @Override
    public String toString() {
        return value + " " + unit.getSymbol();
    }
}
