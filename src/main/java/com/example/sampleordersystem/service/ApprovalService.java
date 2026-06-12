package com.example.sampleordersystem.service;

import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.model.inventory.Stock;
import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.production.ProductionSchedule;
import com.example.sampleordersystem.repository.OrderRepository;
import com.example.sampleordersystem.repository.PendingShipmentStockRepository;
import com.example.sampleordersystem.repository.ProductionScheduleRepository;
import com.example.sampleordersystem.repository.StockRepository;

public class ApprovalService {

    private final OrderRepository orderRepo;
    private final StockRepository stockRepo;
    private final PendingShipmentStockRepository pendingRepo;
    private final ProductionScheduleRepository prodScheduleRepo;

    public ApprovalService(OrderRepository orderRepo,
                           StockRepository stockRepo,
                           PendingShipmentStockRepository pendingRepo,
                           ProductionScheduleRepository prodScheduleRepo) {
        this.orderRepo = orderRepo;
        this.stockRepo = stockRepo;
        this.pendingRepo = pendingRepo;
        this.prodScheduleRepo = prodScheduleRepo;
    }

    public void approve(String orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new IllegalStateException("RESERVED 상태의 주문만 승인할 수 있습니다: " + order.getStatus());
        }

        Stock stock = stockRepo.findBySampleId(order.getSampleId())
                .orElseThrow(() -> new IllegalStateException("재고 정보를 찾을 수 없습니다: " + order.getSampleId()));

        if (stock.getQuantity() >= order.getQuantity()) {
            stock.subtract(order.getQuantity());
            stockRepo.update(stock);

            PendingShipmentStock pending = pendingRepo.findBySampleId(order.getSampleId())
                    .orElseThrow(() -> new IllegalStateException("배송대기 재고를 찾을 수 없습니다: " + order.getSampleId()));
            pending.add(order.getQuantity());
            pendingRepo.update(pending);

            orderRepo.updateStatus(orderId, OrderStatus.CONFIRMED);
        } else {
            orderRepo.updateStatus(orderId, OrderStatus.PRODUCING);
            int deficit = order.getQuantity() - stock.getQuantity();
            prodScheduleRepo.save(new ProductionSchedule(null, orderId, order.getSampleId(), deficit, 0));
        }
    }

    public void reject(String orderId) {
        orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        orderRepo.updateStatus(orderId, OrderStatus.REJECTED);
    }
}
