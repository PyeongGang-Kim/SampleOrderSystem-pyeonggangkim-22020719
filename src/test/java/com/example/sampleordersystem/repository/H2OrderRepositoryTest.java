package com.example.sampleordersystem.repository;

import com.example.sampleordersystem.db.SchemaInitializer;
import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.repository.impl.H2OrderRepository;
import com.example.sampleordersystem.repository.impl.H2SampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2OrderRepositoryTest {

    private Connection conn;
    private OrderRepository repo;
    private Long sampleId;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_order;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);
        Sample sample = new H2SampleRepository(conn).save(new Sample(null, "시료A", 10.0, 0.9));
        sampleId = sample.getId();
        repo = new H2OrderRepository(conn);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS production_schedules");
            stmt.execute("DROP TABLE IF EXISTS orders");
            stmt.execute("DROP TABLE IF EXISTS pending_shipment_stocks");
            stmt.execute("DROP TABLE IF EXISTS stocks");
            stmt.execute("DROP TABLE IF EXISTS samples");
        }
        conn.close();
    }

    @Test
    void 주문을_저장하고_ID로_조회할_수_있다() {
        Order saved = repo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        Optional<Order> found = repo.findById("20240115_0001");
        assertTrue(found.isPresent());
        assertEquals("홍길동", found.get().getCustomerName());
        assertEquals(OrderStatus.RESERVED, found.get().getStatus());
    }

    @Test
    void 상태별로_주문을_조회할_수_있다() {
        repo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        repo.save(new Order("20240115_0002", sampleId, "김철수", 50));
        repo.updateStatus("20240115_0002", OrderStatus.CONFIRMED);

        List<Order> reserved = repo.findByStatus(OrderStatus.RESERVED);
        List<Order> confirmed = repo.findByStatus(OrderStatus.CONFIRMED);

        assertEquals(1, reserved.size());
        assertEquals(1, confirmed.size());
    }

    @Test
    void 주문_상태를_변경할_수_있다() {
        repo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        repo.updateStatus("20240115_0001", OrderStatus.CONFIRMED);
        Order updated = repo.findById("20240115_0001").orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void 날짜_prefix로_주문_수를_집계할_수_있다() {
        repo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        repo.save(new Order("20240115_0002", sampleId, "김철수", 50));
        repo.save(new Order("20240116_0001", sampleId, "이영희", 30));

        assertEquals(2, repo.countByDatePrefix("20240115"));
        assertEquals(1, repo.countByDatePrefix("20240116"));
    }

    @Test
    void 존재하지_않는_ID_조회_시_빈_Optional을_반환한다() {
        assertTrue(repo.findById("99999999_9999").isEmpty());
    }
}
