package com.example.sampleordersystem.repository.impl;

import com.example.sampleordersystem.model.production.ProductionSchedule;
import com.example.sampleordersystem.repository.ProductionScheduleRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class H2ProductionScheduleRepository implements ProductionScheduleRepository {

    private final Connection conn;

    public H2ProductionScheduleRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public ProductionSchedule save(ProductionSchedule schedule) {
        String sql = "INSERT INTO production_schedules (order_id, sample_id, target_quantity, produced_quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, schedule.getOrderId());
            ps.setLong(2, schedule.getSampleId());
            ps.setInt(3, schedule.getTargetQuantity());
            ps.setInt(4, schedule.getProducedQuantity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new ProductionSchedule(keys.getLong(1), schedule.getOrderId(),
                            schedule.getSampleId(), schedule.getTargetQuantity(),
                            schedule.getProducedQuantity(), schedule.getCreatedAt());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("생산 스케쥴 저장 실패", e);
        }
        throw new RuntimeException("생산 스케쥴 저장 후 ID 획득 실패");
    }

    @Override
    public Optional<ProductionSchedule> findById(Long id) {
        String sql = "SELECT id, order_id, sample_id, target_quantity, produced_quantity, created_at FROM production_schedules WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("생산 스케쥴 조회 실패", e);
        }
        return Optional.empty();
    }

    @Override
    public List<ProductionSchedule> findAllOrderByCreatedAt() {
        String sql = "SELECT id, order_id, sample_id, target_quantity, produced_quantity, created_at FROM production_schedules ORDER BY created_at ASC";
        List<ProductionSchedule> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("생산 스케쥴 전체 조회 실패", e);
        }
        return result;
    }

    @Override
    public ProductionSchedule update(ProductionSchedule schedule) {
        String sql = "UPDATE production_schedules SET produced_quantity = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, schedule.getProducedQuantity());
            ps.setLong(2, schedule.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("생산 스케쥴 수정 실패", e);
        }
        return schedule;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM production_schedules WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("생산 스케쥴 삭제 실패", e);
        }
    }

    private ProductionSchedule mapRow(ResultSet rs) throws SQLException {
        return new ProductionSchedule(
                rs.getLong("id"),
                rs.getString("order_id"),
                rs.getLong("sample_id"),
                rs.getInt("target_quantity"),
                rs.getInt("produced_quantity"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
