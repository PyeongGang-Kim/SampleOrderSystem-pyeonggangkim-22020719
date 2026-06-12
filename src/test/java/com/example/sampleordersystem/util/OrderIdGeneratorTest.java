package com.example.sampleordersystem.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderIdGeneratorTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id VARCHAR(50) PRIMARY KEY,
                    sample_id BIGINT NOT NULL,
                    customer_name VARCHAR(255) NOT NULL,
                    quantity INT NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS orders");
        }
        conn.close();
    }

    @Test
    void 당일_첫_주문은_0001로_시작한다() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 15);
        String id = OrderIdGenerator.generate(date, conn);
        assertEquals("20240115_0001", id);
    }

    @Test
    void 기존_주문이_있으면_시퀀스가_증가한다() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 15);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO orders(id, sample_id, customer_name, quantity, status) VALUES('20240115_0001', 1, 'test', 1, 'RESERVED')");
            stmt.execute("INSERT INTO orders(id, sample_id, customer_name, quantity, status) VALUES('20240115_0002', 1, 'test', 1, 'RESERVED')");
        }
        String id = OrderIdGenerator.generate(date, conn);
        assertEquals("20240115_0003", id);
    }

    @Test
    void 날짜가_바뀌면_시퀀스가_리셋된다() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO orders(id, sample_id, customer_name, quantity, status) VALUES('20240115_0001', 1, 'test', 1, 'RESERVED')");
        }
        LocalDate newDate = LocalDate.of(2024, 1, 16);
        String id = OrderIdGenerator.generate(newDate, conn);
        assertEquals("20240116_0001", id);
    }

    @Test
    void ID_형식은_YYYYMMDD_NNNN이다() throws Exception {
        LocalDate date = LocalDate.of(2024, 12, 31);
        String id = OrderIdGenerator.generate(date, conn);
        assertTrue(id.matches("\\d{8}_\\d{4}"), "형식 불일치: " + id);
    }
}
