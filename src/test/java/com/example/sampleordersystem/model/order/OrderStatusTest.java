package com.example.sampleordersystem.model.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {

    @Test
    void RESERVED는_CONFIRMED로_전환_가능하다() {
        assertTrue(OrderStatus.RESERVED.canTransitionTo(OrderStatus.CONFIRMED));
    }

    @Test
    void RESERVED는_PRODUCING으로_전환_가능하다() {
        assertTrue(OrderStatus.RESERVED.canTransitionTo(OrderStatus.PRODUCING));
    }

    @Test
    void RESERVED는_REJECTED로_전환_가능하다() {
        assertTrue(OrderStatus.RESERVED.canTransitionTo(OrderStatus.REJECTED));
    }

    @Test
    void RESERVED는_RELEASE로_전환_불가하다() {
        assertFalse(OrderStatus.RESERVED.canTransitionTo(OrderStatus.RELEASE));
    }

    @Test
    void PRODUCING은_CONFIRMED로_전환_가능하다() {
        assertTrue(OrderStatus.PRODUCING.canTransitionTo(OrderStatus.CONFIRMED));
    }

    @Test
    void PRODUCING은_RESERVED로_전환_불가하다() {
        assertFalse(OrderStatus.PRODUCING.canTransitionTo(OrderStatus.RESERVED));
    }

    @Test
    void CONFIRMED는_RELEASE로_전환_가능하다() {
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.RELEASE));
    }

    @Test
    void CONFIRMED는_RESERVED로_전환_불가하다() {
        assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.RESERVED));
    }

    @Test
    void RELEASE는_모든_상태로_전환_불가하다() {
        for (OrderStatus next : OrderStatus.values()) {
            assertFalse(OrderStatus.RELEASE.canTransitionTo(next), "RELEASE → " + next + " 는 불가해야 합니다");
        }
    }

    @Test
    void REJECTED는_모든_상태로_전환_불가하다() {
        for (OrderStatus next : OrderStatus.values()) {
            assertFalse(OrderStatus.REJECTED.canTransitionTo(next), "REJECTED → " + next + " 는 불가해야 합니다");
        }
    }
}
