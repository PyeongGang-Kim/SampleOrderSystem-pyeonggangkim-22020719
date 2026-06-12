package com.example.sampleordersystem.repository;

import com.example.sampleordersystem.model.inventory.Stock;

import java.util.Optional;

public interface StockRepository {
    Stock save(Stock stock);
    Optional<Stock> findBySampleId(Long sampleId);
    Stock update(Stock stock);
}
