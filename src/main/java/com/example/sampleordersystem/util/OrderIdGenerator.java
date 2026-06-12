package com.example.sampleordersystem.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrderIdGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generate(LocalDate date, Connection conn) {
        String prefix = date.format(FORMATTER);
        int next = getLastSequence(prefix, conn) + 1;
        return String.format("%s_%04d", prefix, next);
    }

    private static int getLastSequence(String prefix, Connection conn) {
        String sql = "SELECT MAX(CAST(SUBSTRING(id, 10) AS INT)) FROM orders WHERE id LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "_%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getObject(1) != null) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("주문 ID 시퀀스 조회 실패", e);
        }
        return 0;
    }
}
