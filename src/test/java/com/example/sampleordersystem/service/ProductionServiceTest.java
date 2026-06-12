package com.example.sampleordersystem.service;

import com.example.sampleordersystem.db.SchemaInitializer;
import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.model.inventory.Stock;
import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.production.ProductionSchedule;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.repository.impl.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class ProductionServiceTest {

    private Connection conn;
    private ProductionService productionService;
    private H2ProductionScheduleRepository prodScheduleRepo;
    private H2StockRepository stockRepo;
    private H2PendingShipmentStockRepository pendingRepo;
    private H2OrderRepository orderRepo;
    private H2SampleRepository sampleRepo;
    private Long sampleId;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_svc_production;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);

        sampleRepo = new H2SampleRepository(conn);
        Sample sample = sampleRepo.save(new Sample(null, "시료A", 60.0, 0.9));
        sampleId = sample.getId();

        orderRepo = new H2OrderRepository(conn);
        stockRepo = new H2StockRepository(conn);
        pendingRepo = new H2PendingShipmentStockRepository(conn);
        prodScheduleRepo = new H2ProductionScheduleRepository(conn);

        productionService = new ProductionService(prodScheduleRepo, stockRepo, pendingRepo, orderRepo, sampleRepo);
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
    void 생산_진행_시_생산량이_재고에_반영된다() {
        // prodRate=60, yield=0.9, 90%기준=0.81, 1분당 생산: 60/(0.81) ≈ 74.07 (버림 = 74)
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 500));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0001", sampleId, 500, 0));

        productionService.advance(1);

        int stock = stockRepo.findBySampleId(sampleId).orElseThrow().getQuantity();
        assertTrue(stock > 0, "1분 후 재고는 0보다 커야 한다");
    }

    @Test
    void 생산_완료_시_주문이_CONFIRMED로_전환된다() {
        // prodRate=60, yield=0.9 → 1분당 60/(0.81)≈74개, targetQuantity=50이면 1분에 완료
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 50));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0001", sampleId, 50, 0));

        productionService.advance(1);

        assertEquals(OrderStatus.CONFIRMED, orderRepo.findById("20240115_0001").orElseThrow().getStatus());
        assertEquals(0, prodScheduleRepo.findAllOrderByCreatedAt().size());
    }

    @Test
    void 생산_완료_시_생산된_수량이_배송대기재고에_반영된다() {
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 50));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0001", sampleId, 50, 0));

        productionService.advance(1);

        assertEquals(50, pendingRepo.findBySampleId(sampleId).orElseThrow().getQuantity());
    }
}
