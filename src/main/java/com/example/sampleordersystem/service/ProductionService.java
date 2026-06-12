package com.example.sampleordersystem.service;

import com.example.sampleordersystem.model.inventory.PendingShipmentStock;
import com.example.sampleordersystem.model.inventory.Stock;
import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.order.OrderStatus;
import com.example.sampleordersystem.model.production.ProductionSchedule;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.repository.OrderRepository;
import com.example.sampleordersystem.repository.PendingShipmentStockRepository;
import com.example.sampleordersystem.repository.ProductionScheduleRepository;
import com.example.sampleordersystem.repository.SampleRepository;
import com.example.sampleordersystem.repository.StockRepository;

import java.util.List;

public class ProductionService {

    private final ProductionScheduleRepository prodScheduleRepo;
    private final StockRepository stockRepo;
    private final PendingShipmentStockRepository pendingRepo;
    private final OrderRepository orderRepo;
    private final SampleRepository sampleRepo;

    public ProductionService(ProductionScheduleRepository prodScheduleRepo,
                             StockRepository stockRepo,
                             PendingShipmentStockRepository pendingRepo,
                             OrderRepository orderRepo,
                             SampleRepository sampleRepo) {
        this.prodScheduleRepo = prodScheduleRepo;
        this.stockRepo = stockRepo;
        this.pendingRepo = pendingRepo;
        this.orderRepo = orderRepo;
        this.sampleRepo = sampleRepo;
    }

    public List<ProductionSchedule> getSchedules() {
        return prodScheduleRepo.findAllOrderByCreatedAt();
    }

    public void advance(int minutes) {
        for (ProductionSchedule schedule : prodScheduleRepo.findAllOrderByCreatedAt()) {
            Stock stock = produce(schedule, minutes);
            if (schedule.isComplete()) {
                completeProduction(schedule, stock);
            } else {
                prodScheduleRepo.update(schedule);
            }
        }
    }

    private Stock produce(ProductionSchedule schedule, int minutes) {
        Sample sample = sampleRepo.findById(schedule.getSampleId())
                .orElseThrow(() -> new IllegalStateException("시료를 찾을 수 없습니다: " + schedule.getSampleId()));
        int produced = (int) (sample.getProdRate() * sample.getYield() * 0.9 * minutes);
        schedule.addProduced(produced);

        Stock stock = stockRepo.findBySampleId(schedule.getSampleId())
                .orElseThrow(() -> new IllegalStateException("재고를 찾을 수 없습니다: " + schedule.getSampleId()));
        stock.add(produced);
        stockRepo.update(stock);
        return stock;
    }

    private void completeProduction(ProductionSchedule schedule, Stock stock) {
        int orderQty = orderRepo.findById(schedule.getOrderId())
                .orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다: " + schedule.getOrderId()))
                .getQuantity();

        PendingShipmentStock pending = pendingRepo.findBySampleId(schedule.getSampleId())
                .orElseThrow(() -> new IllegalStateException("배송대기 재고를 찾을 수 없습니다: " + schedule.getSampleId()));
        pending.add(orderQty);
        pendingRepo.update(pending);

        stock.subtract(orderQty);
        stockRepo.update(stock);

        orderRepo.updateStatus(schedule.getOrderId(), OrderStatus.CONFIRMED);
        prodScheduleRepo.deleteById(schedule.getId());
    }
}
