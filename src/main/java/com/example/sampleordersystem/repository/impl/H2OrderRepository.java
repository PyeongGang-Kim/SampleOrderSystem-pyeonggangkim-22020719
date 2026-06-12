package com.example.sampleordersystem.repository.impl;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.repository.OrderRepository;
import com.example.sampleordersystem.util.OrderIdGenerator;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class H2OrderRepository implements OrderRepository {

    private final Connection conn;

    public H2OrderRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO orders (id, sample_id, customer_name, quantity, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getId());
            ps.setLong(2, order.getSampleId());
            ps.setString(3, order.getCustomerName());
            ps.setInt(4, order.getQuantity());
            ps.setString(5, order.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("주문 저장 실패", e);
        }
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        String sql = "SELECT id, sample_id, customer_name, quantity, status, created_at FROM orders WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("주문 조회 실패", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT id, sample_id, customer_name, quantity, status, created_at FROM orders ORDER BY created_at";
        List<Order> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("주문 전체 조회 실패", e);
        }
        return result;
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        String sql = "SELECT id, sample_id, customer_name, quantity, status, created_at FROM orders WHERE status = ? ORDER BY created_at";
        List<Order> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("주문 상태별 조회 실패", e);
        }
        return result;
    }

    @Override
    public void updateStatus(String id, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("주문 상태 변경 실패", e);
        }
    }

    @Override
    public int countByDatePrefix(String datePrefix) {
        String sql = "SELECT COUNT(*) FROM orders WHERE id LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, datePrefix + "_%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("주문 수 조회 실패", e);
        }
        return 0;
    }

    @Override
    public String generateNextOrderId(LocalDate date) {
        return OrderIdGenerator.generate(date, conn);
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        long rawSampleId = rs.getLong("sample_id");
        Long sampleId = rs.wasNull() ? null : rawSampleId;
        return new Order(
                rs.getString("id"),
                sampleId,
                rs.getString("customer_name"),
                rs.getInt("quantity"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
