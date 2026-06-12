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

    public abstract boolean canTransitionTo(OrderStatus next);
}
