package com.example.sampleordersystem.controller;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.service.OrderService;
import com.example.sampleordersystem.service.SampleService;
import com.example.sampleordersystem.view.OrderView;

public class OrderController {

    private final OrderService orderSvc;
    private final SampleService sampleSvc;
    private final OrderView view;

    public OrderController(OrderService orderSvc, SampleService sampleSvc, OrderView view) {
        this.orderSvc = orderSvc;
        this.sampleSvc = sampleSvc;
        this.view = view;
    }

    public void run() {
        System.out.println("\n[시료 주문 등록]");
        Long sampleId = view.promptSampleId();
        try {
            Sample sample = sampleSvc.findById(sampleId)
                    .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시료입니다: " + sampleId));
            String customerName = view.promptCustomerName();
            int quantity = view.promptQuantity();
            if (view.confirmOrder(sample.getName(), customerName, quantity)) {
                Order order = orderSvc.createOrder(sampleId, customerName, quantity);
                view.showSuccess("주문 등록 완료 - 주문 ID: " + order.getId()
                        + " | 시료: " + sample.getName()
                        + " | 수량: " + quantity);
            } else {
                System.out.println("주문이 취소되었습니다.");
            }
        } catch (Exception e) {
            view.showError(e.getMessage());
        }
    }
}
