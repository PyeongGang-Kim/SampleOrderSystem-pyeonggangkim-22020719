package com.example.sampleordersystem.controller;

import com.example.sampleordersystem.service.ProductionService;
import com.example.sampleordersystem.view.HomeView;

public class HomeController {

    private final ProductionService productionSvc;
    private final HomeView view;
    private final SampleController sampleCtrl;
    private final OrderController orderCtrl;
    private final ApprovalController approvalCtrl;
    private final MonitorController monitorCtrl;
    private final ProductionController productionCtrl;
    private final ShippingController shippingCtrl;

    public HomeController(ProductionService productionSvc, HomeView view,
                          SampleController sampleCtrl, OrderController orderCtrl,
                          ApprovalController approvalCtrl, MonitorController monitorCtrl,
                          ProductionController productionCtrl, ShippingController shippingCtrl) {
        this.productionSvc = productionSvc;
        this.view = view;
        this.sampleCtrl = sampleCtrl;
        this.orderCtrl = orderCtrl;
        this.approvalCtrl = approvalCtrl;
        this.monitorCtrl = monitorCtrl;
        this.productionCtrl = productionCtrl;
        this.shippingCtrl = shippingCtrl;
    }

    public void run() {
        while (true) {
            view.showMenu();
            String input = view.readInput();
            if (input.startsWith("생산 명령")) {
                handleAdvance(input);
                continue;
            }
            switch (input) {
                case "1" -> sampleCtrl.run();
                case "2" -> orderCtrl.run();
                case "3" -> approvalCtrl.run();
                case "4" -> monitorCtrl.run();
                case "5" -> productionCtrl.run();
                case "6" -> shippingCtrl.run();
                case "0" -> { System.out.println("시스템을 종료합니다."); return; }
                default -> view.showError("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void handleAdvance(String input) {
        try {
            String[] parts = input.trim().split("\\s+");
            if (parts.length < 3) {
                view.showError("사용법: 생산 명령 [분]  예) 생산 명령 10");
                return;
            }
            int minutes = Integer.parseInt(parts[2]);
            if (minutes <= 0) { view.showError("1 이상의 분을 입력해주세요."); return; }
            productionSvc.advance(minutes);
            view.showAdvanceResult(minutes);
        } catch (NumberFormatException e) {
            view.showError("분 값은 숫자로 입력해주세요.");
        } catch (Exception e) {
            view.showError(e.getMessage());
        }
    }
}
