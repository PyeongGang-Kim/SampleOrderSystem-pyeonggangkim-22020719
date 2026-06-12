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

class ShippingServiceTest {

    private Connection conn;
    private ShippingService shippingService;
    private H2OrderRepository orderRepo;
    private H2PendingShipmentStockRepository pendingRepo;
    private Long sampleId;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_svc_shipping;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);

        Sample sample = new H2SampleRepository(conn).save(new Sample(null, "시료A", 10.0, 0.9));
        sampleId = sample.getId();

        orderRepo = new H2OrderRepository(conn);
        pendingRepo = new H2PendingShipmentStockRepository(conn);
        new H2StockRepository(conn).save(new Stock(sampleId, 0));
        pendingRepo.save(new PendingShipmentStock(sampleId, 200));

        shippingService = new ShippingService(orderRepo, pendingRepo);
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
    void CONFIRMED_주문을_출고하면_RELEASE_상태로_전환된다() {
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        orderRepo.updateStatus("20240115_0001", OrderStatus.CONFIRMED);

        shippingService.release("20240115_0001");

        assertEquals(OrderStatus.RELEASE, orderRepo.findById("20240115_0001").orElseThrow().getStatus());
    }

    @Test
    void 출고_시_배송대기_재고에서_주문_수량이_차감된다() {
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        orderRepo.updateStatus("20240115_0001", OrderStatus.CONFIRMED);

        shippingService.release("20240115_0001");

        assertEquals(100, pendingRepo.findBySampleId(sampleId).orElseThrow().getQuantity());
    }

    @Test
    void CONFIRMED가_아닌_주문_출고_시도_시_예외가_발생한다() {
        orderRepo.save(new Order("20240115_0001", sampleId, "홍길동", 100));
        assertThrows(IllegalStateException.class, () -> shippingService.release("20240115_0001"));
    }
}
