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
import java.util.List;

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
        // prodRate=60, yield=0.9 → 1분당 60*0.9*0.9 = 48개
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 500));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0001", sampleId, 500, 0));

        productionService.advance(1);

        int stock = stockRepo.findBySampleId(sampleId).orElseThrow().getQuantity();
        assertEquals(48, stock, "1분 후 재고는 48이어야 한다");
    }

    @Test
    void 생산_완료_시_주문이_CONFIRMED로_전환된다() {
        // prodRate=60, yield=0.9 → 1분당 48개, targetQuantity=40이면 1분에 완료
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 40));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0001", sampleId, 40, 0));

        productionService.advance(1);

        assertEquals(OrderStatus.CONFIRMED, orderRepo.findById("20240115_0001").orElseThrow().getStatus());
        assertEquals(0, prodScheduleRepo.findAllOrderByCreatedAt().size());
    }

    @Test
    void 생산_완료_시_생산된_수량이_배송대기재고에_반영된다() {
        // 생산 완료 시 targetQuantity만큼 PendingShipmentStock에 반영
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 40));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0001", sampleId, 40, 0));

        productionService.advance(1);

        assertEquals(40, pendingRepo.findBySampleId(sampleId).orElseThrow().getQuantity());
    }

    @Test
    void 부족분_생산_완료_시_배송대기재고는_주문_원래_수량이다() {
        // stock=50, orderQty=100, targetQuantity=50(부족분)
        // 생산 완료 후 PendingShipmentStock = 100 (주문 원래 수량), Stock = 47 (50+97-100)
        stockRepo.save(new Stock(sampleId, 50));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0001", sampleId, 50, 0));

        // prodRate=60, yield=0.9 → 2분에 (int)(60*0.9*0.9*2)=97개 생산 → targetQty(50) 초과 완료
        productionService.advance(2);

        assertEquals(OrderStatus.CONFIRMED, orderRepo.findById("20240115_0001").orElseThrow().getStatus());
        assertEquals(100, pendingRepo.findBySampleId(sampleId).orElseThrow().getQuantity(),
                "배송대기 재고는 주문 원래 수량(100)이어야 한다 (부족분 50이 아님)");
        assertEquals(0, prodScheduleRepo.findAllOrderByCreatedAt().size());
    }

    @Test
    void 스케줄_목록은_등록_순서대로_반환된다() {
        // 생산 현황 화면에서 첫 번째 항목이 현재 생산 중인 항목이어야 한다
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 1000));
        orderRepo.save(new Order("20240115_0002", sampleId, "김철수", 2000));
        orderRepo.updateStatus("20240115_0001", OrderStatus.PRODUCING);
        orderRepo.updateStatus("20240115_0002", OrderStatus.PRODUCING);
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0001", sampleId, 1000, 0));
        prodScheduleRepo.save(new ProductionSchedule(null, "20240115_0002", sampleId, 2000, 0));

        List<ProductionSchedule> schedules = productionService.getSchedules();

        assertEquals(2, schedules.size());
        assertEquals("20240115_0001", schedules.get(0).getOrderId(), "먼저 등록된 주문이 첫 번째여야 한다");
        assertEquals("20240115_0002", schedules.get(1).getOrderId());
    }
}
