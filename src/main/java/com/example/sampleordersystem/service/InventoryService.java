package com.example.sampleordersystem.service;

import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.model.inventory.Stock;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.repository.OrderRepository;
import com.example.sampleordersystem.repository.PendingShipmentStockRepository;
import com.example.sampleordersystem.repository.StockRepository;

import java.util.Set;

public class InventoryService {

    private static final Set<OrderStatus> ACTIVE_STATUSES =
            Set.of(OrderStatus.RESERVED, OrderStatus.PRODUCING, OrderStatus.CONFIRMED);

    private final StockRepository stockRepo;
    private final PendingShipmentStockRepository pendingRepo;
    private final OrderRepository orderRepo;

    public InventoryService(StockRepository stockRepo,
                            PendingShipmentStockRepository pendingRepo,
                            OrderRepository orderRepo) {
        this.stockRepo = stockRepo;
        this.pendingRepo = pendingRepo;
        this.orderRepo = orderRepo;
    }

    public StockStatus getStockStatus(Long sampleId) {
        int stockQty = stockRepo.findBySampleId(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다: " + sampleId))
                .getQuantity();
        int pendingQty = pendingRepo.findBySampleId(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("배송대기 재고를 찾을 수 없습니다: " + sampleId))
                .getQuantity();
        int totalQty = stockQty + pendingQty;
        if (totalQty == 0) return StockStatus.DEPLETED;

        int orderedQty = getOrderedQuantity(sampleId);
        if (totalQty < orderedQty) return StockStatus.LOW;
        return StockStatus.SUFFICIENT;
    }

    private int getOrderedQuantity(Long sampleId) {
        return ACTIVE_STATUSES.stream()
                .flatMap(status -> orderRepo.findByStatus(status).stream())
                .filter(order -> sampleId.equals(order.getSampleId()))
                .mapToInt(order -> order.getQuantity())
                .sum();
    }

    public InventorySummary getInventorySummary(Long sampleId) {
        Stock stock = stockRepo.findBySampleId(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다: " + sampleId));
        PendingShipmentStock pending = pendingRepo.findBySampleId(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("배송대기 재고를 찾을 수 없습니다: " + sampleId));
        int orderedQty = getOrderedQuantity(sampleId);
        return new InventorySummary(stock.getQuantity(), pending.getQuantity(), orderedQty);
    }

    public void addStock(Long sampleId, int amount) {
        Stock stock = stockRepo.findBySampleId(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다: " + sampleId));
        stock.add(amount);
        stockRepo.update(stock);
    }

    public enum StockStatus {
        DEPLETED, LOW, SUFFICIENT
    }

    public record InventorySummary(int stockQuantity, int pendingQuantity, int orderedQuantity) {}
}
