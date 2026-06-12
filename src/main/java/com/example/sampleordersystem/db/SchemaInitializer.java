package com.example.sampleordersystem.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    public static void init(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS samples (
                    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name      VARCHAR(255) NOT NULL,
                    prod_rate DOUBLE       NOT NULL,
                    yield     DOUBLE       NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS stocks (
                    sample_id BIGINT PRIMARY KEY,
                    quantity  INT NOT NULL DEFAULT 0,
                    FOREIGN KEY (sample_id) REFERENCES samples(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pending_shipment_stocks (
                    sample_id BIGINT PRIMARY KEY,
                    quantity  INT NOT NULL DEFAULT 0,
                    FOREIGN KEY (sample_id) REFERENCES samples(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id            VARCHAR(50) PRIMARY KEY,
                    sample_id     BIGINT       NOT NULL,
                    customer_name VARCHAR(255) NOT NULL,
                    quantity      INT          NOT NULL,
                    status        VARCHAR(20)  NOT NULL,
                    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (sample_id) REFERENCES samples(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS production_schedules (
                    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                    order_id          VARCHAR(50) NOT NULL,
                    sample_id         BIGINT      NOT NULL,
                    target_quantity   INT         NOT NULL,
                    produced_quantity INT         NOT NULL DEFAULT 0,
                    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (order_id)  REFERENCES orders(id),
                    FOREIGN KEY (sample_id) REFERENCES samples(id)
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("스키마 초기화 실패", e);
        }
    }
}
