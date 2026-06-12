package com.example.sampleordersystem.service;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.repository.OrderRepository;

import java.time.LocalDate;
import java.util.List;

public class OrderService {

    private final OrderRepository orderRepo;

    public OrderService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    public Order createOrder(Long sampleId, String customerName, int quantity) {
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
}
