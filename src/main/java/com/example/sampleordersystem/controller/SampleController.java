package com.example.sampleordersystem.controller;

import com.example.sampleordersystem.model.sample.Sample;
import com.example.sampleordersystem.service.InventoryService;
import com.example.sampleordersystem.service.SampleService;
import com.example.sampleordersystem.util.Paginator;
import com.example.sampleordersystem.view.SampleView;

import java.util.ArrayList;
import java.util.List;

public class SampleController {

    private final SampleService sampleSvc;
    private final InventoryService inventorySvc;
    private final SampleView view;

    public SampleController(SampleService sampleSvc, InventoryService inventorySvc, SampleView view) {
        this.sampleSvc = sampleSvc;
        this.inventorySvc = inventorySvc;
        this.view = view;
    }

    public void run() {
        while (true) {
            view.showSubMenu();
            String choice = view.readChoice();
            switch (choice) {
                case "1" -> registerSample();
                case "2" -> listSamples();
                case "3" -> searchSamples();
                case "4" -> updateSample();
                case "5" -> deleteSample();
                case "0" -> { return; }
                default -> view.showError("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void registerSample() {
        System.out.println("\n[시료 등록]");
        String name = view.promptName();
        double prodRate = view.promptProdRate();
        double yield = view.promptYield();
        try {
            Sample sample = sampleSvc.createSample(name, prodRate, yield);
            view.showSuccess("시료 등록 완료 - ID: " + sample.getId() + ", 이름: " + sample.getName());
        } catch (Exception e) {
            view.showError(e.getMessage());
        }
    }

    private void listSamples() {
        List<Sample> all = sampleSvc.getAllSamples();
        int page = 1;
        while (true) {
            int total = Paginator.totalPages(all);
            List<Sample> pageItems = Paginator.paginate(all, page);
            view.showSampleTable(buildHeaders(), buildRows(pageItems), page, total);
            if (total <= 1) { pressEnterToContinue(); return; }
            String nav = view.readPageNav(page, total);
            if ("0".equals(nav)) return;
            if ("p".equals(nav) && page > 1) page--;
            else if ("n".equals(nav) && page < total) page++;
        }
    }

    private void searchSamples() {
        String keyword = view.promptKeyword();
        List<Sample> results = sampleSvc.searchSamples(keyword);
        int page = 1;
        while (true) {
            int total = Paginator.totalPages(results);
            List<Sample> pageItems = Paginator.paginate(results, page);
            view.showSampleTable(buildHeaders(), buildRows(pageItems), page, total);
            if (total <= 1) { pressEnterToContinue(); return; }
            String nav = view.readPageNav(page, total);
            if ("0".equals(nav)) return;
            if ("p".equals(nav) && page > 1) page--;
            else if ("n".equals(nav) && page < total) page++;
        }
    }

    private void updateSample() {
        System.out.println("\n[시료 수정]");
        Long id = view.promptSampleId("수정할 시료 ID: ");
        try {
            Sample existing = sampleSvc.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("시료를 찾을 수 없습니다: " + id));
            System.out.println("현재 값 - 이름: " + existing.getName()
                    + ", 생산속도: " + existing.getProdRate()
                    + ", 수율: " + existing.getYield());
            String name = view.promptName();
            double prodRate = view.promptProdRate();
            double yield = view.promptYield();
            Sample updated = sampleSvc.updateSample(id, name, prodRate, yield);
            view.showSuccess("시료 수정 완료 - " + updated.getName());
        } catch (Exception e) {
            view.showError(e.getMessage());
        }
    }

    private void deleteSample() {
        System.out.println("\n[시료 삭제]");
        Long id = view.promptSampleId("삭제할 시료 ID: ");
        try {
            Sample sample = sampleSvc.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("시료를 찾을 수 없습니다: " + id));
            if (view.confirmDelete(sample.getName())) {
                sampleSvc.deleteSample(id);
                view.showSuccess("시료 삭제 완료 - " + sample.getName());
            }
        } catch (Exception e) {
            view.showError(e.getMessage());
        }
    }

    private List<String> buildHeaders() {
        return List.of("ID", "이름", "생산속도(분당)", "수율", "재고 수량", "배송대기", "상태");
    }

    private List<List<String>> buildRows(List<Sample> samples) {
        List<List<String>> rows = new ArrayList<>();
        for (Sample s : samples) {
            String stock = "-";
            String pending = "-";
            String status = "-";
            try {
                InventoryService.InventorySummary summary = inventorySvc.getInventorySummary(s.getId());
                stock = String.valueOf(summary.stockQuantity());
                pending = String.valueOf(summary.pendingQuantity());
                status = switch (inventorySvc.getStockStatus(s.getId())) {
                    case DEPLETED -> "고갈";
                    case LOW -> "부족";
                    case SUFFICIENT -> "여유";
                };
            } catch (Exception ignored) {}
            rows.add(List.of(
                    String.valueOf(s.getId()),
                    s.getName(),
                    String.valueOf(s.getProdRate()),
                    String.valueOf(s.getYield()),
                    stock, pending, status
            ));
        }
        return rows;
    }

    private void pressEnterToContinue() {
        com.example.sampleordersystem.util.ConsoleUtil.readLine("엔터를 누르면 돌아갑니다.");
    }
}
