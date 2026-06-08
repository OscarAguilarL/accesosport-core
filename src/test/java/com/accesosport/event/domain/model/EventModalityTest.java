package com.accesosport.event.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventModalityTest {

    private static final UUID EVENT_ID = UUID.randomUUID();

    // --- create ---

    @Test
    void create_startsWithZeroRegisteredCount() {
        EventModality m = EventModality.create(EVENT_ID, "10K", new BigDecimal("10"), DistanceUnit.KM, new BigDecimal("150"));

        assertThat(m.getRegisteredCount()).isZero();
    }

    @Test
    void create_assignsRandomId() {
        EventModality a = EventModality.create(EVENT_ID, "10K", new BigDecimal("10"), DistanceUnit.KM, BigDecimal.ZERO);
        EventModality b = EventModality.create(EVENT_ID, "10K", new BigDecimal("10"), DistanceUnit.KM, BigDecimal.ZERO);

        assertThat(a.getId()).isNotEqualTo(b.getId());
    }

    @Test
    void create_storesAllFields() {
        BigDecimal distance = new BigDecimal("21.097");
        BigDecimal price = new BigDecimal("350.00");

        EventModality m = EventModality.create(EVENT_ID, "21K Medio Maratón", distance, DistanceUnit.KM, price);

        assertThat(m.getEventId()).isEqualTo(EVENT_ID);
        assertThat(m.getName()).isEqualTo("21K Medio Maratón");
        assertThat(m.getDistance()).isEqualByComparingTo(distance);
        assertThat(m.getDistanceUnit()).isEqualTo(DistanceUnit.KM);
        assertThat(m.getPrice()).isEqualByComparingTo(price);
    }

    @Test
    void create_withMilesUnit_storesCorrectUnit() {
        EventModality m = EventModality.create(EVENT_ID, "5 Miles", new BigDecimal("5"), DistanceUnit.MI, BigDecimal.ZERO);

        assertThat(m.getDistanceUnit()).isEqualTo(DistanceUnit.MI);
    }

    @Test
    void create_withZeroPrice_isValid() {
        EventModality m = EventModality.create(EVENT_ID, "5K Gratis", new BigDecimal("5"), DistanceUnit.KM, BigDecimal.ZERO);

        assertThat(m.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- create: validaciones ---

    @Test
    void create_withBlankName_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                EventModality.create(EVENT_ID, "  ", new BigDecimal("10"), DistanceUnit.KM, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_withNullName_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                EventModality.create(EVENT_ID, null, new BigDecimal("10"), DistanceUnit.KM, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_withZeroDistance_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                EventModality.create(EVENT_ID, "10K", BigDecimal.ZERO, DistanceUnit.KM, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_withNegativeDistance_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                EventModality.create(EVENT_ID, "10K", new BigDecimal("-1"), DistanceUnit.KM, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_withNullDistanceUnit_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                EventModality.create(EVENT_ID, "10K", new BigDecimal("10"), null, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_withNegativePrice_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                EventModality.create(EVENT_ID, "10K", new BigDecimal("10"), DistanceUnit.KM, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- hasRegistrations ---

    @Test
    void hasRegistrations_falseWhenRegisteredCountIsZero() {
        EventModality m = reconstitute(0);

        assertThat(m.hasRegistrations()).isFalse();
    }

    @Test
    void hasRegistrations_trueWhenAtLeastOneRegistered() {
        EventModality m = reconstitute(1);

        assertThat(m.hasRegistrations()).isTrue();
    }

    // --- helper ---

    private EventModality reconstitute(int registeredCount) {
        return EventModality.reconstitute(
                UUID.randomUUID(), EVENT_ID, "10K", new BigDecimal("10"),
                DistanceUnit.KM, new BigDecimal("100"), null, registeredCount
        );
    }
}
