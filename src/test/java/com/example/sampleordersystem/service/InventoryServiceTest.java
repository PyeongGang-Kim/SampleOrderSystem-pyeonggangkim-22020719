package com.example.sampleordersystem.service;

import com.example.sampleordersystem.db.SchemaInitializer;
import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.model.inventory.Stock;
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

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    private Connection conn;
    private InventoryService inventoryService;
    private H2StockRepository stockRepo;
    private H2PendingShipmentStockRepository pendingRepo;
    private H2OrderRepository orderRepo;
    private Long sampleId;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_svc_inventory;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);

        Sample sample = new H2SampleRepository(conn).save(new Sample(null, "시료A", 10.0, 0.9));
        sampleId = sample.getId();

        stockRepo = new H2StockRepository(conn);
        pendingRepo = new H2PendingShipmentStockRepository(conn);
        orderRepo = new H2OrderRepository(conn);
        inventoryService = new InventoryService(stockRepo, pendingRepo, orderRepo);
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
    void 재고_초기화_후_수량을_조회할_수_있다() {
        stockRepo.save(new Stock(sampleId, 100));
        pendingRepo.save(new PendingShipmentStock(sampleId, 20));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 50));

        InventoryService.InventorySummary summary = inventoryService.getInventorySummary(sampleId);

        assertEquals(100, summary.stockQuantity());
        assertEquals(20, summary.pendingQuantity());
        assertEquals(50, summary.orderedQuantity());
    }

    @Test
    void 재고_수량이_0이면_고갈_상태이다() {
        stockRepo.save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));

        assertEquals(InventoryService.StockStatus.DEPLETED, inventoryService.getStockStatus(sampleId));
    }

    @Test
    void 재고가_주문된_수량보다_부족하면_부족_상태이다() {
        stockRepo.save(new Stock(sampleId, 50));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));

        assertEquals(InventoryService.StockStatus.LOW, inventoryService.getStockStatus(sampleId));
    }

    @Test
    void 재고가_주문된_수량_이상이면_여유_상태이다() {
        stockRepo.save(new Stock(sampleId, 100));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));

        assertEquals(InventoryService.StockStatus.SUFFICIENT, inventoryService.getStockStatus(sampleId));
    }

    @Test
    void 활성_주문이_없으면_재고가_있을_때_여유_상태이다() {
        stockRepo.save(new Stock(sampleId, 1));
        pendingRepo.save(new PendingShipmentStock(sampleId, 0));

        assertEquals(InventoryService.StockStatus.SUFFICIENT, inventoryService.getStockStatus(sampleId));
    }

    @Test
    void 재고를_추가할_수_있다() {
        stockRepo.save(new Stock(sampleId, 50));
        inventoryService.addStock(sampleId, 30);
        assertEquals(80, stockRepo.findBySampleId(sampleId).orElseThrow().getQuantity());
    }
}
