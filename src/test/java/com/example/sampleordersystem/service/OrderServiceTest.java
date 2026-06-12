package com.example.sampleordersystem.service;

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

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private Connection conn;
    private OrderService orderService;
    private Long sampleId;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_svc_order;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);

        Sample sample = new H2SampleRepository(conn).save(new Sample(null, "시료A", 10.0, 0.9));
        sampleId = sample.getId();

        H2OrderRepository orderRepo = new H2OrderRepository(conn);
        H2SampleRepository sampleRepo = new H2SampleRepository(conn);
        orderService = new OrderService(orderRepo, sampleRepo);
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
    void 주문을_생성하면_RESERVED_상태로_저장된다() {
        Order order = orderService.createOrder(sampleId, "홍길동", 100);
        assertNotNull(order.getId());
        assertEquals(OrderStatus.RESERVED, order.getStatus());
        assertEquals("홍길동", order.getCustomerName());
        assertEquals(100, order.getQuantity());
    }

    @Test
    void 주문_ID는_YYYYMMDD_NNNN_형식으로_생성된다() {
        Order order = orderService.createOrder(sampleId, "홍길동", 100);
        assertTrue(order.getId().matches("\\d{8}_\\d{4}"));
    }

    @Test
    void 같은_날_두_번째_주문의_시퀀스는_0002이다() {
        Order first = orderService.createOrder(sampleId, "홍길동", 100);
        Order second = orderService.createOrder(sampleId, "김철수", 50);

        String firstSeq = first.getId().split("_")[1];
        String secondSeq = second.getId().split("_")[1];

        assertEquals("0001", firstSeq);
        assertEquals("0002", secondSeq);
    }

    @Test
    void RESERVED_주문_목록을_조회할_수_있다() {
        orderService.createOrder(sampleId, "홍길동", 100);
        orderService.createOrder(sampleId, "김철수", 50);
        List<Order> reserved = orderService.getOrdersByStatus(OrderStatus.RESERVED);
        assertEquals(2, reserved.size());
    }

    @Test
    void 전체_주문_목록을_조회할_수_있다() {
        orderService.createOrder(sampleId, "홍길동", 100);
        orderService.createOrder(sampleId, "김철수", 50);
        List<Order> all = orderService.getAllOrders();
        assertEquals(2, all.size());
    }

    @Test
    void 주문ID로_검색하면_해당_주문이_반환된다() {
        Order created = orderService.createOrder(sampleId, "홍길동", 100);
        orderService.createOrder(sampleId, "김철수", 50);

        List<Order> result = orderService.searchOrders(created.getId());

        assertEquals(1, result.size());
        assertEquals(created.getId(), result.get(0).getId());
    }

    @Test
    void 고객명_일부로_검색하면_해당_주문들이_반환된다() {
        orderService.createOrder(sampleId, "홍길동", 100);
        orderService.createOrder(sampleId, "홍두깨", 50);
        orderService.createOrder(sampleId, "김철수", 30);

        List<Order> result = orderService.searchOrders("홍");

        assertEquals(2, result.size());
    }

    @Test
    void 검색어와_일치하는_주문이_없으면_빈_목록이_반환된다() {
        orderService.createOrder(sampleId, "홍길동", 100);

        List<Order> result = orderService.searchOrders("없는고객");

        assertTrue(result.isEmpty());
    }
}
