package com.example.sampleordersystem.repository.impl;

import com.example.sampleordersystem.model.inventory.Stock;
import com.example.sampleordersystem.repository.StockRepository;

import java.sql.*;
import java.util.Optional;

public class H2StockRepository implements StockRepository {

    private final Connection conn;

    public H2StockRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Stock save(Stock stock) {
        String sql = "INSERT INTO stocks (sample_id, quantity) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, stock.getSampleId());
            ps.setInt(2, stock.getQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("재고 저장 실패", e);
        }
        return stock;
    }

    @Override
    public Optional<Stock> findBySampleId(Long sampleId) {
        String sql = "SELECT sample_id, quantity FROM stocks WHERE sample_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sampleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(new Stock(rs.getLong("sample_id"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("재고 조회 실패", e);
        }
        return Optional.empty();
    }

    @Override
    public Stock update(Stock stock) {
        String sql = "UPDATE stocks SET quantity = ? WHERE sample_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stock.getQuantity());
            ps.setLong(2, stock.getSampleId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("재고 수정 실패", e);
        }
        return stock;
    }
}
