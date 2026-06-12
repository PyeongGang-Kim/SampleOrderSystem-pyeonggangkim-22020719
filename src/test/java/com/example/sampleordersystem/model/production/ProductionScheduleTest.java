package com.example.sampleordersystem.model.production;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductionScheduleTest {

    private ProductionSchedule schedule(int produced, int target) {
        return new ProductionSchedule(1L, "20240115_0001", 1L, target, produced);
    }

    @Test
    void 생산량이_목표_미달이면_완료_아니다() {
        assertFalse(schedule(50, 100).isComplete());
    }

    @Test
    void 생산량이_목표와_같으면_완료이다() {
        assertTrue(schedule(100, 100).isComplete());
    }

    @Test
    void 생산량이_목표_초과이면_완료이다() {
        assertTrue(schedule(101, 100).isComplete());
    }

    @Test
    void addProduced_호출_시_생산량이_누적된다() {
        ProductionSchedule s = schedule(0, 100);
        s.addProduced(30);
        s.addProduced(40);
        assertEquals(70, s.getProducedQuantity());
    }
}
