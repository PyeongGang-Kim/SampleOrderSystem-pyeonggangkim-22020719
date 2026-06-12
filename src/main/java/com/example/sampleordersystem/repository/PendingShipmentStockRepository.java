package com.example.sampleordersystem.repository;

import com.example.sampleordersystem.model.inventory.PendingShipmentStock;

import java.util.Optional;

public interface PendingShipmentStockRepository {
    PendingShipmentStock save(PendingShipmentStock stock);
    Optional<PendingShipmentStock> findBySampleId(Long sampleId);
    PendingShipmentStock update(PendingShipmentStock stock);
}
