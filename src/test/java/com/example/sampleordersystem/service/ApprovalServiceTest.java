package com.example.sampleordersystem.service;

import com.example.sampleordersystem.db.SchemaInitializer;
import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.model.inventory.Stock;
import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.repository.impl.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalServiceTest {

    private Connection conn;
    private ApprovalService approvalService;
    private H2OrderRepository orderRepo;
    private H2StockRepository stockRepo;
    private H2PendingShipmentStockRepository pendingRepo;
    private H2ProductionScheduleRepository prodScheduleRepo;
    private Long sampleId;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_svc_approval;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);

        Sample sample = new H2SampleRepository(conn).save(new Sample(null, "시료A", 10.0, 0.9));
        sampleId = sample.getId();

        orderRepo = new H2OrderRepository(conn);
        stockRepo = new H2StockRepository(conn);
        pendingRepo = new H2PendingShipmentStockRepository(conn);
        prodScheduleRepo = new H2ProductionScheduleRepository(conn);

        approvalService = new ApprovalService(orderRepo, stockRepo, pendingRepo, prodScheduleRepo);
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
    void 재고_충분_시_주문이_CONFIRMED로_전환되고_재고가_차감된다() {
        stockRepo.save(new Stock(sampleId, 200));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));

        approvalService.approve("20240115_0001");

        assertEquals(OrderStatus.CONFIRMED, orderRepo.findById("20240115_0001").orElseThrow().getStatus());
        assertEquals(100, stockRepo.findBySampleId(sampleId).orElseThrow().getQuantity());
        assertEquals(100, pendingRepo.findBySampleId(sampleId).orElseThrow().getQuantity());
    }

    @Test
    void 재고_부족_시_주문이_PRODUCING으로_전환되고_생산스케쥴이_등록된다() {
        stockRepo.save(new Stock(sampleId, 50));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));

        approvalService.approve("20240115_0001");

        assertEquals(OrderStatus.PRODUCING, orderRepo.findById("20240115_0001").orElseThrow().getStatus());
        assertEquals(50, stockRepo.findBySampleId(sampleId).orElseThrow().getQuantity());
        assertEquals(1, prodScheduleRepo.findAllOrderByCreatedAt().size());
    }

    @Test
    void 재고가_0일_때_주문이_PRODUCING으로_전환된다() {
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));

        approvalService.approve("20240115_0001");

        assertEquals(OrderStatus.PRODUCING, orderRepo.findById("20240115_0001").orElseThrow().getStatus());
    }

    @Test
    void 주문_거절_시_REJECTED로_전환된다() {
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        approvalService.reject("20240115_0001");
        assertEquals(OrderStatus.REJECTED, orderRepo.findById("20240115_0001").orElseThrow().getStatus());
    }

    @Test
    void RESERVED가_아닌_주문_승인_시도_시_예외가_발생한다() {
        stockRepo.save(new Stock(sampleId, 200));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        orderRepo.updateStatus("20240115_0001", OrderStatus.CONFIRMED);

        assertThrows(IllegalStateException.class, () -> approvalService.approve("20240115_0001"));
    }
}
