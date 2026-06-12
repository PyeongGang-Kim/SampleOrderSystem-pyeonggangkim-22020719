package com.example.sampleordersystem.repository;

import com.example.sampleordersystem.model.production.ProductionSchedule;

import java.util.List;
import java.util.Optional;

public interface ProductionScheduleRepository {
    ProductionSchedule save(ProductionSchedule schedule);
    Optional<ProductionSchedule> findById(Long id);
    List<ProductionSchedule> findAllOrderByCreatedAt();
    ProductionSchedule update(ProductionSchedule schedule);
    void deleteById(Long id);
}
