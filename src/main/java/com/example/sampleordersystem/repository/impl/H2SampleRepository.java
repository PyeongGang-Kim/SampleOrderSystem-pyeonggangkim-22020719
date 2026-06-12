package com.example.sampleordersystem.repository.impl;

import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.repository.SampleRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class H2SampleRepository implements SampleRepository {

    private final Connection conn;

    public H2SampleRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Sample save(Sample sample) {
        String sql = "INSERT INTO samples (name, prod_rate, yield) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sample.getName());
            ps.setDouble(2, sample.getProdRate());
            ps.setDouble(3, sample.getYield());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Sample(keys.getLong(1), sample.getName(), sample.getProdRate(), sample.getYield());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("시료 저장 실패", e);
        }
        throw new RuntimeException("시료 저장 후 ID 획득 실패");
    }

    @Override
    public Optional<Sample> findById(Long id) {
        String sql = "SELECT id, name, prod_rate, yield FROM samples WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("시료 조회 실패", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Sample> findAll() {
        String sql = "SELECT id, name, prod_rate, yield FROM samples ORDER BY id";
        List<Sample> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("시료 전체 조회 실패", e);
        }
        return result;
    }

    @Override
    public Sample update(Sample sample) {
        String sql = "UPDATE samples SET name = ?, prod_rate = ?, yield = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sample.getName());
            ps.setDouble(2, sample.getProdRate());
            ps.setDouble(3, sample.getYield());
            ps.setLong(4, sample.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("시료 수정 실패", e);
        }
        return sample;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM samples WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("시료 삭제 실패", e);
        }
    }

    private Sample mapRow(ResultSet rs) throws SQLException {
        return new Sample(rs.getLong("id"), rs.getString("name"),
                rs.getDouble("prod_rate"), rs.getDouble("yield"));
    }
}
