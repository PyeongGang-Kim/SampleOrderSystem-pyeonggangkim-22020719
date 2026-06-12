package com.example.sampleordersystem.repository.impl;

import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.repository.PendingShipmentStockRepository;

import java.sql.*;
import java.util.Optional;

public class H2PendingShipmentStockRepository implements PendingShipmentStockRepository {

    private final Connection conn;

    public H2PendingShipmentStockRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public PendingShipmentStock save(PendingShipmentStock stock) {
        String sql = "INSERT INTO pending_shipment_stocks (sample_id, quantity) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, stock.getSampleId());
            ps.setInt(2, stock.getQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("배송대기 재고 저장 실패", e);
        }
        return stock;
    }

    @Override
    public Optional<PendingShipmentStock> findBySampleId(Long sampleId) {
        String sql = "SELECT sample_id, quantity FROM pending_shipment_stocks WHERE sample_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sampleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(new PendingShipmentStock(rs.getLong("sample_id"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("배송대기 재고 조회 실패", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteBySampleId(Long sampleId) {
        String sql = "DELETE FROM pending_shipment_stocks WHERE sample_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sampleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("배송대기 재고 삭제 실패", e);
        }
    }

    @Override
    public PendingShipmentStock update(PendingShipmentStock stock) {
        String sql = "UPDATE pending_shipment_stocks SET quantity = ? WHERE sample_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stock.getQuantity());
            ps.setLong(2, stock.getSampleId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("배송대기 재고 수정 실패", e);
        }
        return stock;
    }
}
