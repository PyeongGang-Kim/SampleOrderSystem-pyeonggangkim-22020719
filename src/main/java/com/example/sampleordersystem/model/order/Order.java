package com.example.sampleordersystem.model.order;

import java.time.LocalDateTime;

public class Order {

    private final String id;
    private final Long sampleId;
    private final String customerName;
    private final int quantity;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    public Order(String id, Long sampleId, String customerName, int quantity) {
        this.id = id;
        this.sampleId = sampleId;
        this.customerName = customerName;
        this.quantity = quantity;
        this.status = OrderStatus.RESERVED;
        this.createdAt = LocalDateTime.now();
    }

    public Order(String id, Long sampleId, String customerName, int quantity,
                 OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.sampleId = sampleId;
        this.customerName = customerName;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void changeStatus(OrderStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new IllegalStateException(
                String.format("주문 상태 전환 불가: %s → %s", this.status, next));
        }
        this.status = next;
    }

    public String getId() { return id; }
    public Long getSampleId() { return sampleId; }
    public String getCustomerName() { return customerName; }
    public int getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
