package com.example.sampleordersystem.repository;

import com.example.sampleordersystem.db.SchemaInitializer;
import com.example.sampleordersystem.model.sample.Sample;
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

class H2SampleRepositoryTest {

    private Connection conn;
    private SampleRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_sample;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.init(conn);
        repo = new H2SampleRepository(conn);
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
    void 시료를_저장하고_ID로_조회할_수_있다() {
        Sample saved = repo.save(new Sample(null, "시료A", 10.0, 0.9));
        assertNotNull(saved.getId());
        Optional<Sample> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("시료A", found.get().getName());
    }

    @Test
    void 전체_시료_목록을_조회할_수_있다() {
        repo.save(new Sample(null, "시료A", 10.0, 0.9));
        repo.save(new Sample(null, "시료B", 20.0, 0.8));
        List<Sample> all = repo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void 존재하지_않는_ID_조회_시_빈_Optional을_반환한다() {
        Optional<Sample> result = repo.findById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void 시료_속성을_수정할_수_있다() {
        Sample saved = repo.save(new Sample(null, "시료A", 10.0, 0.9));
        saved.setName("시료A_수정");
        saved.setProdRate(15.0);
        repo.update(saved);
        Sample updated = repo.findById(saved.getId()).orElseThrow();
        assertEquals("시료A_수정", updated.getName());
        assertEquals(15.0, updated.getProdRate());
    }

    @Test
    void 시료를_삭제할_수_있다() {
        Sample saved = repo.save(new Sample(null, "시료A", 10.0, 0.9));
        repo.deleteById(saved.getId());
        assertTrue(repo.findById(saved.getId()).isEmpty());
    }
}
