package com.accesosport.event.domain.model;

import com.accesosport.event.domain.exception.NoCapacityException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventCapacityTest {

    private static final UUID EVENT_ID = UUID.randomUUID();

    // --- create ---

    @Test
    void create_startsWithZeroReserved() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, 100);

        assertThat(capacity.getReserved()).isZero();
    }

    @Test
    void create_storesMaxCapacity() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, 50);

        assertThat(capacity.getMaxCapacity()).isEqualTo(50);
    }

    @Test
    void create_withNullMaxCapacity_meansUnlimited() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, null);

        assertThat(capacity.getMaxCapacity()).isNull();
    }

    // --- hasAvailability ---

    @Test
    void hasAvailability_trueWhenReservedLessThanMax() {
        EventCapacity capacity = EventCapacity.reconstitute(EVENT_ID, 9, 10);

        assertThat(capacity.hasAvailability()).isTrue();
    }

    @Test
    void hasAvailability_falseWhenFull() {
        EventCapacity capacity = EventCapacity.reconstitute(EVENT_ID, 10, 10);

        assertThat(capacity.hasAvailability()).isFalse();
    }

    @Test
    void hasAvailability_alwaysTrueWhenUnlimited() {
        EventCapacity capacity = EventCapacity.reconstitute(EVENT_ID, 999_999, null);

        assertThat(capacity.hasAvailability()).isTrue();
    }

    // --- getAvailable ---

    @Test
    void getAvailable_returnsCorrectDifference() {
        EventCapacity capacity = EventCapacity.reconstitute(EVENT_ID, 3, 10);

        assertThat(capacity.getAvailable()).isEqualTo(7);
    }

    @Test
    void getAvailable_returnsZeroWhenFull() {
        EventCapacity capacity = EventCapacity.reconstitute(EVENT_ID, 10, 10);

        assertThat(capacity.getAvailable()).isZero();
    }

    @Test
    void getAvailable_returnsMaxIntWhenUnlimited() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, null);

        assertThat(capacity.getAvailable()).isEqualTo(Integer.MAX_VALUE);
    }

    // --- reserve ---

    @Test
    void reserve_incrementsReserved() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, 10);

        capacity.reserve();

        assertThat(capacity.getReserved()).isEqualTo(1);
    }

    @Test
    void reserve_throwsNoCapacityExceptionWhenFull() {
        EventCapacity capacity = EventCapacity.reconstitute(EVENT_ID, 10, 10);

        assertThatThrownBy(capacity::reserve)
                .isInstanceOf(NoCapacityException.class);
    }

    @Test
    void reserve_doesNotThrowWhenUnlimited() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, null);

        capacity.reserve();

        assertThat(capacity.getReserved()).isEqualTo(1);
    }

    // --- release ---

    @Test
    void release_decrementsReserved() {
        EventCapacity capacity = EventCapacity.reconstitute(EVENT_ID, 5, 10);

        capacity.release();

        assertThat(capacity.getReserved()).isEqualTo(4);
    }

    @Test
    void release_doesNotGoBelowZero() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, 10); // reserved = 0

        capacity.release();

        assertThat(capacity.getReserved()).isZero();
    }

    // --- updateMaxCapacity ---

    @Test
    void updateMaxCapacity_updatesValue() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, 100);

        capacity.updateMaxCapacity(200);

        assertThat(capacity.getMaxCapacity()).isEqualTo(200);
    }

    @Test
    void updateMaxCapacity_allowsNull_makesUnlimited() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, 100);

        capacity.updateMaxCapacity(null);

        assertThat(capacity.getMaxCapacity()).isNull();
        assertThat(capacity.hasAvailability()).isTrue();
    }

    @Test
    void updateMaxCapacity_throwsWhenZero() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, 100);

        assertThatThrownBy(() -> capacity.updateMaxCapacity(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateMaxCapacity_throwsWhenNegative() {
        EventCapacity capacity = EventCapacity.create(EVENT_ID, 100);

        assertThatThrownBy(() -> capacity.updateMaxCapacity(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
