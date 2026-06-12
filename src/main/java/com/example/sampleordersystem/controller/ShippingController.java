package com.example.sampleordersystem.controller;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.service.OrderService;
import com.example.sampleordersystem.service.SampleService;
import com.example.sampleordersystem.service.ShippingService;
import com.example.sampleordersystem.util.Paginator;
import com.example.sampleordersystem.view.ShippingView;

import java.util.ArrayList;
import java.util.List;

public class ShippingController {

    private final ShippingService shippingSvc;
    private final OrderService orderSvc;
    private final SampleService sampleSvc;
    private final ShippingView view;

    public ShippingController(ShippingService shippingSvc, OrderService orderSvc,
                               SampleService sampleSvc, ShippingView view) {
        this.shippingSvc = shippingSvc;
        this.orderSvc = orderSvc;
        this.sampleSvc = sampleSvc;
        this.view = view;
    }

    public void run() {
        List<Order> confirmed = orderSvc.getOrdersByStatus(OrderStatus.CONFIRMED);
        int page = 1;
        while (true) {
            int total = Paginator.totalPages(confirmed);
            List<Order> pageItems = Paginator.paginate(confirmed, page);
            view.showConfirmedTable(buildHeaders(), buildRows(pageItems), page, total);
            String nav = view.readPageNav(page, total);
            if ("0".equals(nav)) return;
            if ("p".equals(nav)) { if (page > 1) page--; continue; }
            if ("n".equals(nav)) { if (page < total) page++; continue; }
            processShipping(nav, confirmed);
            confirmed = orderSvc.getOrdersByStatus(OrderStatus.CONFIRMED);
        }
    }

    private void processShipping(String orderId, List<Order> confirmed) {
        Order order = confirmed.stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElse(null);
        if (order == null) {
            view.showError("해당 주문 ID를 찾을 수 없습니다: " + orderId);
            return;
        }
        try {
            String sampleName = order.getSampleId() == null ? "(삭제됨)" :
                    sampleSvc.findById(order.getSampleId()).map(Sample::getName).orElse("(알 수 없음)");
            shippingSvc.release(order.getId());
            view.showShippingResult(order.getId(), sampleName, order.getCustomerName(), order.getQuantity());
        } catch (Exception e) {
            view.showError(e.getMessage());
        }
    }

    private List<String> buildHeaders() {
        return List.of("주문 ID", "시료명", "고객명", "수량", "등록일시");
    }

    private List<List<String>> buildRows(List<Order> orders) {
        List<List<String>> rows = new ArrayList<>();
        for (Order o : orders) {
            String sampleName = o.getSampleId() == null ? "(삭제됨)" :
                    sampleSvc.findById(o.getSampleId()).map(Sample::getName).orElse("(알 수 없음)");
            rows.add(List.of(o.getId(), sampleName, o.getCustomerName(),
                    String.valueOf(o.getQuantity()), o.getCreatedAt().toString()));
        }
        return rows;
    }
}
