package com.example.sampleordersystem.controller;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.service.ApprovalService;
import com.example.sampleordersystem.service.InventoryService;
import com.example.sampleordersystem.service.OrderService;
import com.example.sampleordersystem.service.SampleService;
import com.example.sampleordersystem.util.Paginator;
import com.example.sampleordersystem.view.ApprovalView;

import java.util.List;

public class ApprovalController {

    private final ApprovalService approvalSvc;
    private final InventoryService inventorySvc;
    private final OrderService orderSvc;
    private final SampleService sampleSvc;
    private final ApprovalView view;

    public ApprovalController(ApprovalService approvalSvc, InventoryService inventorySvc,
                               OrderService orderSvc, SampleService sampleSvc, ApprovalView view) {
        this.approvalSvc = approvalSvc;
        this.inventorySvc = inventorySvc;
        this.orderSvc = orderSvc;
        this.sampleSvc = sampleSvc;
        this.view = view;
    }

    public void run() {
        while (true) {
            view.showSubMenu();
            String choice = view.readChoice();
            switch (choice) {
                case "1" -> showReservedOrders();
                case "2" -> showAllOrders();
                case "3" -> searchOrders();
                case "0" -> { return; }
                default -> view.showError("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void showReservedOrders() {
        List<Order> reserved = orderSvc.getOrdersByStatus(OrderStatus.RESERVED);
        int page = 1;
        while (true) {
            int total = Paginator.totalPages(reserved);
            List<Order> pageItems = Paginator.paginate(reserved, page);
            view.showReservedTable(OrderTableFormatter.DEFAULT_HEADERS, buildReservedRows(pageItems), page, total);
            String nav = view.readPageNav(page, total);
            if ("0".equals(nav)) return;
            if ("p".equals(nav)) { if (page > 1) page--; continue; }
            if ("n".equals(nav)) { if (page < total) page++; continue; }
            processOrderSelection(nav, reserved);
            reserved = orderSvc.getOrdersByStatus(OrderStatus.RESERVED);
        }
    }

    private void showAllOrders() {
        List<Order> orders = orderSvc.getAllOrders();
        int page = 1;
        while (true) {
            int total = Paginator.totalPages(orders);
            List<Order> pageItems = Paginator.paginate(orders, page);
            view.showAllOrdersTable(OrderTableFormatter.FULL_HEADERS, buildAllRows(pageItems), page, total);
            String nav = view.readPageNavReadOnly(page, total);
            if ("0".equals(nav)) return;
            if ("p".equals(nav)) { if (page > 1) page--; }
            else if ("n".equals(nav)) { if (page < total) page++; }
        }
    }

    private void searchOrders() {
        String keyword = view.promptSearchKeyword();
        if (keyword.isBlank()) {
            view.showError("검색어를 입력해주세요.");
            return;
        }
        List<Order> orders = orderSvc.searchOrders(keyword);
        int page = 1;
        while (true) {
            int total = Paginator.totalPages(orders);
            List<Order> pageItems = Paginator.paginate(orders, page);
            view.showSearchResultTable(OrderTableFormatter.FULL_HEADERS, buildAllRows(pageItems), page, total);
            String nav = view.readPageNavReadOnly(page, total);
            if ("0".equals(nav)) return;
            if ("p".equals(nav)) { if (page > 1) page--; }
            else if ("n".equals(nav)) { if (page < total) page++; }
        }
    }

    private void processOrderSelection(String orderId, List<Order> reserved) {
        Order order = reserved.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElse(null);
        if (order == null) {
            view.showError("해당 주문 ID를 찾을 수 없습니다: " + orderId);
            return;
        }
        String choice = view.promptApproveOrReject();
        switch (choice) {
            case "1" -> handleApprove(order);
            case "2" -> handleReject(order);
            case "0" -> {}
            default -> view.showError("올바른 선택을 입력해주세요.");
        }
    }

    private void handleApprove(Order order) {
        try {
            String sampleName = sampleSvc.resolveSampleName(order.getSampleId());
            int currentStock = inventorySvc.getInventorySummary(order.getSampleId()).stockQuantity();
            view.showStockInfo(sampleName, currentStock, order.getQuantity());
            if (view.confirmApprove()) {
                approvalSvc.approve(order.getId());
                view.showSuccess("주문 승인 완료 - " + order.getId());
            }
        } catch (Exception e) {
            view.showError(e.getMessage());
        }
    }

    private void handleReject(Order order) {
        try {
            approvalSvc.reject(order.getId());
            view.showSuccess("주문 거절 완료 - " + order.getId());
        } catch (Exception e) {
            view.showError(e.getMessage());
        }
    }

    private List<List<String>> buildReservedRows(List<Order> orders) {
        return OrderTableFormatter.buildRows(orders, sampleSvc);
    }

    private List<List<String>> buildAllRows(List<Order> orders) {
        return OrderTableFormatter.buildRowsWithStatus(orders, sampleSvc);
    }
}
