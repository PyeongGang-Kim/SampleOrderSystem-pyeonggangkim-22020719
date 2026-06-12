package com.example.sampleordersystem.model.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryTest {

    // Stock과 PendingShipmentStock 모두 동일한 Inventory 규칙을 따르므로 양쪽 모두 검증
    private Stock stock(int qty) {
        return new Stock(1L, qty);
    }

    private PendingShipmentStock pending(int qty) {
        return new PendingShipmentStock(1L, qty);
    }

    @Test
    void Stock_add_후_수량이_증가한다() {
        Stock s = stock(10);
        s.add(5);
        assertEquals(15, s.getQuantity());
    }

    @Test
    void Stock_subtract_후_수량이_감소한다() {
        Stock s = stock(10);
        s.subtract(3);
        assertEquals(7, s.getQuantity());
    }

    @Test
    void Stock_subtract_잔량_초과_시_예외가_발생한다() {
        Stock s = stock(5);
        assertThrows(IllegalStateException.class, () -> s.subtract(6));
    }

    @Test
    void Stock_add_음수_입력_시_예외가_발생한다() {
        Stock s = stock(10);
        assertThrows(IllegalArgumentException.class, () -> s.add(-1));
    }

    @Test
    void PendingShipmentStock_add_후_수량이_증가한다() {
        PendingShipmentStock p = pending(10);
        p.add(5);
        assertEquals(15, p.getQuantity());
    }

    @Test
    void PendingShipmentStock_subtract_후_수량이_감소한다() {
        PendingShipmentStock p = pending(10);
        p.subtract(3);
        assertEquals(7, p.getQuantity());
    }

    @Test
    void PendingShipmentStock_subtract_잔량_초과_시_예외가_발생한다() {
        PendingShipmentStock p = pending(5);
        assertThrows(IllegalStateException.class, () -> p.subtract(6));
    }

    @Test
    void PendingShipmentStock_add_음수_입력_시_예외가_발생한다() {
        PendingShipmentStock p = pending(10);
        assertThrows(IllegalArgumentException.class, () -> p.add(-1));
    }
}
