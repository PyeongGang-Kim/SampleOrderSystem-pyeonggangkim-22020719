package com.example.sampleordersystem.model.inventory;

public abstract class Inventory {

    private final Long sampleId;
    private int quantity;

    protected Inventory(Long sampleId, int quantity) {
        this.sampleId = sampleId;
        this.quantity = quantity;
    }

    public void add(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("추가 수량은 0보다 커야 합니다: " + amount);
        }
        this.quantity += amount;
    }

    public void subtract(int amount) {
        if (this.quantity < amount) {
            throw new IllegalStateException(
                String.format("재고 부족: 현재 %d, 요청 %d", this.quantity, amount));
        }
        this.quantity -= amount;
    }

    public Long getSampleId() { return sampleId; }
    public int getQuantity() { return quantity; }
}
