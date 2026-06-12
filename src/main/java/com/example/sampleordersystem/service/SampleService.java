package com.example.sampleordersystem.service;

import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.model.inventory.Stock;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.repository.OrderRepository;
import com.example.sampleordersystem.repository.PendingShipmentStockRepository;
import com.example.sampleordersystem.repository.SampleRepository;
import com.example.sampleordersystem.repository.StockRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SampleService {

    private static final Set<OrderStatus> ACTIVE_STATUSES =
            Set.of(OrderStatus.RESERVED, OrderStatus.PRODUCING, OrderStatus.CONFIRMED);

    private final SampleRepository sampleRepo;
    private final OrderRepository orderRepo;
    private final StockRepository stockRepo;
    private final PendingShipmentStockRepository pendingRepo;

    public SampleService(SampleRepository sampleRepo, OrderRepository orderRepo,
                         StockRepository stockRepo, PendingShipmentStockRepository pendingRepo) {
        this.sampleRepo = sampleRepo;
        this.orderRepo = orderRepo;
        this.stockRepo = stockRepo;
        this.pendingRepo = pendingRepo;
    }

    public Sample createSample(String name, double prodRate, double yield) {
        Sample sample = sampleRepo.save(new Sample(null, name, prodRate, yield));
        stockRepo.save(new Stock(sample.getId(), 0));
        pendingRepo.save(new PendingShipmentStock(sample.getId(), 0));
        return sample;
    }

    public List<Sample> getAllSamples() {
        return sampleRepo.findAll();
    }

    public Optional<Sample> findById(Long id) {
        return sampleRepo.findById(id);
    }

    public List<Sample> searchSamples(String keyword) {
        return sampleRepo.findAll().stream()
                .filter(s -> s.getName().contains(keyword) ||
                             String.valueOf(s.getId()).contains(keyword))
                .collect(Collectors.toList());
    }

    public Sample updateSample(Long id, String name, double prodRate, double yield) {
        Sample sample = sampleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("시료를 찾을 수 없습니다: " + id));
        sample.setName(name);
        sample.setProdRate(prodRate);
        sample.setYield(yield);
        return sampleRepo.update(sample);
    }

    public void deleteSample(Long id) {
        boolean hasActiveOrder = ACTIVE_STATUSES.stream()
                .flatMap(status -> orderRepo.findByStatus(status).stream())
                .anyMatch(order -> id.equals(order.getSampleId()));

        if (hasActiveOrder) {
            throw new IllegalStateException("진행 중인 주문이 있어 시료를 삭제할 수 없습니다: " + id);
        }
        pendingRepo.deleteBySampleId(id);
        stockRepo.deleteBySampleId(id);
        sampleRepo.deleteById(id);
    }
}
