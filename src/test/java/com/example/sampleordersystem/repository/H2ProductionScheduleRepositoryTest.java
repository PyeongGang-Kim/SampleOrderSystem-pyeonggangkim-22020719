package com.example.sampleordersystem.repository;

import com.example.sampleordersystem.db.SchemaInitializer;
import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.production.ProductionSchedule;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.repository.impl.H2OrderRepository;
import com.example.sampleordersystem.repository.impl.H2ProductionScheduleRepository;
import com.example.sampleordersystem.repository.impl.H2SampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class H2ProductionScheduleRepositoryTest {

    private Connection conn;
    private ProductionScheduleRepository repo;
    private Long sampleId;
    private String orderId;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_prod;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);
        Sample sample = new H2SampleRepository(conn).save(new Sample(null, "시료A", 10.0, 0.9));
        sampleId = sample.getId();
        Order order = new H2OrderRepository(conn).save(new Order("20240115_0001", sampleId, "홍길동", 100));
        orderId = order.getId();
        repo = new H2ProductionScheduleRepository(conn);
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
    void 생산_스케쥴을_저장하고_ID로_조회할_수_있다() {
        ProductionSchedule saved = repo.save(new ProductionSchedule(null, orderId, sampleId, 100, 0));
        assertNotNull(saved.getId());
        assertTrue(repo.findById(saved.getId()).isPresent());
    }

    @Test
    void 생산_스케쥴을_FIFO_순서로_조회한다() throws Exception {
        String orderId2 = "20240115_0002";
        new H2OrderRepository(conn).save(new Order(orderId2, sampleId, "김철수", 50));

        ProductionSchedule s1 = repo.save(new ProductionSchedule(null, orderId, sampleId, 100, 0));
        Thread.sleep(10);
        ProductionSchedule s2 = repo.save(new ProductionSchedule(null, orderId2, sampleId, 50, 0));

        List<ProductionSchedule> schedules = repo.findAllOrderByCreatedAt();
        assertEquals(2, schedules.size());
        assertEquals(s1.getId(), schedules.get(0).getId());
        assertEquals(s2.getId(), schedules.get(1).getId());
    }

    @Test
    void 생산량을_업데이트할_수_있다() {
        ProductionSchedule saved = repo.save(new ProductionSchedule(null, orderId, sampleId, 100, 0));
        saved.addProduced(50);
        repo.update(saved);
        ProductionSchedule updated = repo.findById(saved.getId()).orElseThrow();
        assertEquals(50, updated.getProducedQuantity());
    }

    @Test
    void 생산_스케쥴을_삭제할_수_있다() {
        ProductionSchedule saved = repo.save(new ProductionSchedule(null, orderId, sampleId, 100, 0));
        repo.deleteById(saved.getId());
        assertTrue(repo.findById(saved.getId()).isEmpty());
    }
}
