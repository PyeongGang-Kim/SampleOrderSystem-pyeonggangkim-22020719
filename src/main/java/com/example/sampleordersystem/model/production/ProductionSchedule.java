package com.example.sampleordersystem.model.production;

import java.time.LocalDateTime;

public class ProductionSchedule {

    private final Long id;
    private final String orderId;
    private final Long sampleId;
    private final int targetQuantity;
    private int producedQuantity;
    private final LocalDateTime createdAt;

    public ProductionSchedule(Long id, String orderId, Long sampleId, int targetQuantity, int producedQuantity) {
        this.id = id;
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.targetQuantity = targetQuantity;
        this.producedQuantity = producedQuantity;
        this.createdAt = LocalDateTime.now();
    }

    public ProductionSchedule(Long id, String orderId, Long sampleId, int targetQuantity,
                               int producedQuantity, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.targetQuantity = targetQuantity;
        this.producedQuantity = producedQuantity;
        this.createdAt = createdAt;
    }

    public boolean isComplete() {
        return producedQuantity >= targetQuantity;
    }

    public void addProduced(int amount) {
        this.producedQuantity += amount;
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public Long getSampleId() { return sampleId; }
    public int getTargetQuantity() { return targetQuantity; }
    public int getProducedQuantity() { return producedQuantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
