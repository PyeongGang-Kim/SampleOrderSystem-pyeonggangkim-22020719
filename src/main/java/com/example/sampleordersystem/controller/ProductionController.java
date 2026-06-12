package com.example.sampleordersystem.controller;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.model.production.ProductionSchedule;
import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.service.ProductionService;
import com.example.sampleordersystem.service.SampleService;
import com.example.sampleordersystem.util.Paginator;
import com.example.sampleordersystem.view.ProductionView;

import java.util.ArrayList;
import java.util.List;

public class ProductionController {

    private final ProductionService productionSvc;
    private final SampleService sampleSvc;
    private final ProductionView view;

    public ProductionController(ProductionService productionSvc, SampleService sampleSvc, ProductionView view) {
        this.productionSvc = productionSvc;
        this.sampleSvc = sampleSvc;
        this.view = view;
    }

    public void run() {
        while (true) {
            view.showSubMenu();
            String choice = view.readChoice();
            switch (choice) {
                case "1" -> showSchedules();
                case "2" -> showPendingOrders();
                case "0" -> { return; }
                default -> view.showError("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void showSchedules() {
        List<ProductionSchedule> schedules = productionSvc.getSchedules();
        List<String> headers = List.of("스케쥴ID", "주문ID", "시료명", "목표 수량", "현재 생산량", "잔여 수량");
        int page = 1;
        while (true) {
            int total = Paginator.totalPages(schedules);
            List<ProductionSchedule> pageItems = Paginator.paginate(schedules, page);
            List<List<String>> rows = buildScheduleRows(pageItems);
            view.showScheduleTable(headers, rows, page, total);
            if (total <= 1) { readEnter(); return; }
            String nav = view.readPageNav(page, total);
            if ("0".equals(nav)) return;
            if ("p".equals(nav) && page > 1) page--;
            else if ("n".equals(nav) && page < total) page++;
        }
    }

    private void showPendingOrders() {
        List<Order> pending = productionSvc.getPendingOrders();
        List<String> headers = List.of("주문ID", "시료명", "주문 수량", "등록 시각");
        List<List<String>> rows = new ArrayList<>();
        for (Order o : pending) {
            String sampleName = o.getSampleId() == null ? "(삭제됨)" :
                    sampleSvc.findById(o.getSampleId()).map(Sample::getName).orElse("(알 수 없음)");
            rows.add(List.of(o.getId(), sampleName,
                    String.valueOf(o.getQuantity()), o.getCreatedAt().toString()));
        }
        view.showPendingOrderTable(headers, rows);
        readEnter();
    }

    private List<List<String>> buildScheduleRows(List<ProductionSchedule> schedules) {
        List<List<String>> rows = new ArrayList<>();
        for (ProductionSchedule s : schedules) {
            String sampleName = sampleSvc.findById(s.getSampleId())
                    .map(Sample::getName).orElse("(알 수 없음)");
            int remaining = s.getTargetQuantity() - s.getProducedQuantity();
            rows.add(List.of(
                    String.valueOf(s.getId()), s.getOrderId(), sampleName,
                    String.valueOf(s.getTargetQuantity()),
                    String.valueOf(s.getProducedQuantity()),
                    String.valueOf(remaining)
            ));
        }
        return rows;
    }

    private void readEnter() {
        com.example.sampleordersystem.util.ConsoleUtil.readLine("엔터를 누르면 돌아갑니다.");
    }
}
