package com.example.sampleordersystem.service;

import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.repository.OrderRepository;
import com.example.sampleordersystem.repository.PendingShipmentStockRepository;

public class ShippingService {

    private final OrderRepository orderRepo;
    private final PendingShipmentStockRepository pendingRepo;

    public ShippingService(OrderRepository orderRepo, PendingShipmentStockRepository pendingRepo) {
        this.orderRepo = orderRepo;
        this.pendingRepo = pendingRepo;
    }

    public void release(String orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("CONFIRMED 상태의 주문만 출고할 수 있습니다: " + order.getStatus());
        }

        PendingShipmentStock pending = pendingRepo.findBySampleId(order.getSampleId())
                .orElseThrow(() -> new IllegalStateException("배송대기 재고를 찾을 수 없습니다: " + order.getSampleId()));
        pending.subtract(order.getQuantity());
        pendingRepo.update(pending);

        orderRepo.updateStatus(orderId, OrderStatus.RELEASE);
    }
}
