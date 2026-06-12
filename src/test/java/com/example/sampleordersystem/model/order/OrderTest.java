package com.example.sampleordersystem.model.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order createOrder() {
        return new Order("20240115_0001", 1L, "홍길동", 100);
    }

    @Test
    void 주문_생성_시_상태는_RESERVED이다() {
        Order order = createOrder();
        assertEquals(OrderStatus.RESERVED, order.getStatus());
    }

    @Test
    void 주문_ID는_변경_불가하다() {
        Order order = createOrder();
        assertEquals("20240115_0001", order.getId());
    }

    @Test
    void 허용된_상태로_전환이_가능하다() {
        Order order = createOrder();
        order.changeStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void 허용되지_않은_상태로_전환하면_예외가_발생한다() {
        Order order = createOrder();
        assertThrows(IllegalStateException.class, () -> order.changeStatus(OrderStatus.RELEASE));
    }

    @Test
    void 주문_생성_시_createdAt이_설정된다() {
        Order order = createOrder();
        assertNotNull(order.getCreatedAt());
    }
}
