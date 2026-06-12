package com.example.sampleordersystem.controller;

import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.service.InventoryService;
import com.example.sampleordersystem.service.OrderService;
import com.example.sampleordersystem.service.SampleService;
import com.example.sampleordersystem.view.MonitorView;

import java.util.ArrayList;
import java.util.List;

public class MonitorController {

    private final InventoryService inventorySvc;
    private final SampleService sampleSvc;
    private final OrderService orderSvc;
    private final MonitorView view;

    public MonitorController(InventoryService inventorySvc, SampleService sampleSvc,
                              OrderService orderSvc, MonitorView view) {
        this.inventorySvc = inventorySvc;
        this.sampleSvc = sampleSvc;
        this.orderSvc = orderSvc;
        this.view = view;
    }

    public void run() {
        while (true) {
            view.showSubMenu();
            String choice = view.readChoice();
            switch (choice) {
                case "1" -> showOrderSummary();
                case "2" -> showInventory();
                case "0" -> { return; }
                default -> view.showError("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void showOrderSummary() {
        int reserved = orderSvc.getOrdersByStatus(OrderStatus.RESERVED).size();
        int producing = orderSvc.getOrdersByStatus(OrderStatus.PRODUCING).size();
        int confirmed = orderSvc.getOrdersByStatus(OrderStatus.CONFIRMED).size();
        int release = orderSvc.getOrdersByStatus(OrderStatus.RELEASE).size();
        view.showOrderSummary(reserved, confirmed, producing, release);
    }

    private void showInventory() {
        List<Sample> samples = sampleSvc.getAllSamples();
        List<String> headers = List.of("시료ID", "시료명", "재고 수량", "배송대기 재고", "총 재고 수량", "주문된 수량", "상태");
        List<List<String>> rows = new ArrayList<>();
        for (Sample s : samples) {
            try {
                InventoryService.InventorySummary summary = inventorySvc.getInventorySummary(s.getId());
                int total = summary.stockQuantity() + summary.pendingQuantity();
                String status = switch (inventorySvc.getStockStatus(s.getId())) {
                    case DEPLETED -> "고갈";
                    case LOW -> "부족";
                    case SUFFICIENT -> "여유";
                };
                rows.add(List.of(
                        String.valueOf(s.getId()), s.getName(),
                        String.valueOf(summary.stockQuantity()),
                        String.valueOf(summary.pendingQuantity()),
                        String.valueOf(total),
                        String.valueOf(summary.orderedQuantity()),
                        status
                ));
            } catch (Exception e) {
                System.err.println("[경고] 재고 조회 중 오류 (시료ID=" + s.getId() + "): " + e.getMessage());
                rows.add(List.of(String.valueOf(s.getId()), s.getName(), "-", "-", "-", "-", "-"));
            }
        }
        view.showInventoryTable(headers, rows);
    }
}
