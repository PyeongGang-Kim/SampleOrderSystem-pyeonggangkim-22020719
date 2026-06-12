package com.example.sampleordersystem.model.order;

import java.util.Set;

public enum OrderStatus {

    RESERVED {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return Set.of(CONFIRMED, PRODUCING, REJECTED).contains(next);
        }
    },
    CONFIRMED {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return next == RELEASE;
        }
    },
    PRODUCING {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return next == CONFIRMED;
        }
    },
    RELEASE {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return false;
        }
    },
    REJECTED {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return false;
        }
    };

    /** 진행 중인 주문으로 간주하는 상태 집합 (삭제 제약 및 재고 계산에 사용) */
    public static final Set<OrderStatus> ACTIVE_STATUSES =
            Set.of(RESERVED, PRODUCING, CONFIRMED);

    public abstract boolean canTransitionTo(OrderStatus next);
}
