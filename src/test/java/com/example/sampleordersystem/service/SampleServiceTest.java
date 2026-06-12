package com.example.sampleordersystem.service;

import com.example.sampleordersystem.db.SchemaInitializer;
import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.repository.impl.H2OrderRepository;
import com.example.sampleordersystem.repository.impl.H2PendingShipmentStockRepository;
import com.example.sampleordersystem.repository.impl.H2SampleRepository;
import com.example.sampleordersystem.repository.impl.H2StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SampleServiceTest {

    private Connection conn;
    private SampleService sampleService;
    private H2OrderRepository orderRepo;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_svc_sample;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);
        H2SampleRepository sampleRepo = new H2SampleRepository(conn);
        orderRepo = new H2OrderRepository(conn);
        H2StockRepository stockRepo = new H2StockRepository(conn);
        H2PendingShipmentStockRepository pendingRepo = new H2PendingShipmentStockRepository(conn);
        sampleService = new SampleService(sampleRepo, orderRepo, stockRepo, pendingRepo);
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
    void 시료를_생성하고_전체_목록을_조회할_수_있다() {
        sampleService.createSample("시료A", 10.0, 0.9);
        sampleService.createSample("시료B", 20.0, 0.8);
        List<Sample> all = sampleService.getAllSamples();
        assertEquals(2, all.size());
    }

    @Test
    void 키워드로_시료를_검색할_수_있다() {
        sampleService.createSample("시료Alpha", 10.0, 0.9);
        sampleService.createSample("시료Beta", 20.0, 0.8);
        List<Sample> result = sampleService.searchSamples("Alpha");
        assertEquals(1, result.size());
        assertEquals("시료Alpha", result.get(0).getName());
    }

    @Test
    void 시료_속성을_수정할_수_있다() {
        Sample created = sampleService.createSample("시료A", 10.0, 0.9);
        Sample updated = sampleService.updateSample(created.getId(), "시료A_수정", 15.0, 0.85);
        assertEquals("시료A_수정", updated.getName());
        assertEquals(15.0, updated.getProdRate());
    }

    @Test
    void 진행중인_주문이_없는_시료는_삭제_가능하다() {
        Sample created = sampleService.createSample("시료A", 10.0, 0.9);
        assertDoesNotThrow(() -> sampleService.deleteSample(created.getId()));
        assertTrue(sampleService.findById(created.getId()).isEmpty());
    }

    @Test
    void RESERVED_주문이_있는_시료는_삭제_불가하다() {
        Sample created = sampleService.createSample("시료A", 10.0, 0.9);
        orderRepo.save(new Order("20240115_0001", created.getId(), "홍길동", 100));
        assertThrows(IllegalStateException.class, () -> sampleService.deleteSample(created.getId()));
    }

    @Test
    void PRODUCING_주문이_있는_시료는_삭제_불가하다() {
        Sample created = sampleService.createSample("시료A", 10.0, 0.9);
        orderRepo.save(new Order("20240115_0001", created.getId(), "홍길동", 100));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        assertThrows(IllegalStateException.class, () -> sampleService.deleteSample(created.getId()));
    }

    @Test
    void CONFIRMED_주문이_있는_시료는_삭제_불가하다() {
        Sample created = sampleService.createSample("시료A", 10.0, 0.9);
        orderRepo.save(new Order("20240115_0001", created.getId(), "홍길동", 100));
        orderRepo.updateStatus("20240115_0001", OrderStatus.CONFIRMED);
        assertThrows(IllegalStateException.class, () -> sampleService.deleteSample(created.getId()));
    }

    @Test
    void RELEASE_주문만_있는_시료는_삭제_가능하다() {
        Sample created = sampleService.createSample("시료A", 10.0, 0.9);
        orderRepo.save(new Order("20240115_0001", created.getId(), "홍길동", 100));
        orderRepo.updateStatus("20240115_0001", OrderStatus.CONFIRMED);
        orderRepo.updateStatus("20240115_0001", OrderStatus.RELEASE);
        assertDoesNotThrow(() -> sampleService.deleteSample(created.getId()));
    }
}
