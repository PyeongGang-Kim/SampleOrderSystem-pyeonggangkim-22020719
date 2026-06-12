package com.example.sampleordersystem.repository;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
    void updateStatus(String id, OrderStatus status);
    int countByDatePrefix(String datePrefix);
    String generateNextOrderId(LocalDate date);
}
