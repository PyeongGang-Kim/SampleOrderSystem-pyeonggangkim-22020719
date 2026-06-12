package com.example.sampleordersystem.service;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.repository.OrderRepository;
import com.example.sampleordersystem.repository.SampleRepository;

import java.time.LocalDate;
import java.util.List;

public class OrderService {

    private final OrderRepository orderRepo;
    private final SampleRepository sampleRepo;

    public OrderService(OrderRepository orderRepo, SampleRepository sampleRepo) {
        this.orderRepo = orderRepo;
        this.sampleRepo = sampleRepo;
    }

    public Order createOrder(Long sampleId, String customerName, int quantity) {
        sampleRepo.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시료입니다: " + sampleId));
        String id = orderRepo.generateNextOrderId(LocalDate.now());
        Order order = new Order(id, sampleId, customerName, quantity);
        return orderRepo.save(order);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepo.findByStatus(status);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public List<Order> searchOrders(String keyword) {
        return orderRepo.findByKeyword(keyword);
    }
}
