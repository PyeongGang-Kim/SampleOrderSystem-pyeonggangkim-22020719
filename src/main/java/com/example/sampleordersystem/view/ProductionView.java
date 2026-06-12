package com.example.sampleordersystem.view;

import com.example.sampleordersystem.util.ConsoleUtil;
import com.example.sampleordersystem.util.TablePrinter;

import java.util.List;

public class ProductionView {

    public void showSubMenu() {
        System.out.println("\n----- 생산 라인 -----");
        System.out.println(" 1. 생산 현황");
        System.out.println(" 2. 대기 주문");
        System.out.println(" 0. 뒤로");
    }

    public String readChoice() {
        return ConsoleUtil.readLine("> ");
    }

    public void showNoCurrentSchedule() {
        System.out.println("\n[생산 현황] 현재 생산 중인 항목이 없습니다.");
    }

    public void showCurrentScheduleDetail(String scheduleId, String orderId, String sampleName,
                                           String targetQty, String producedQty, String remaining) {
        System.out.println("\n[현재 생산 중]");
        System.out.println("  스케줄 ID  : " + scheduleId);
        System.out.println("  주문 ID    : " + orderId);
        System.out.println("  시료명     : " + sampleName);
        System.out.println("  목표 수량  : " + targetQty);
        System.out.println("  현재 생산량: " + producedQty);
        System.out.println("  잔여 수량  : " + remaining);
    }

    public void showScheduleTable(List<String> headers, List<List<String>> rows, int page, int totalPages) {
        System.out.println(String.format("\n[대기 주문 목록] 페이지 %d / %d", page, totalPages == 0 ? 1 : totalPages));
        TablePrinter.print(headers, rows);
    }

    public String readPageNav(int page, int totalPages) {
        return ConsoleUtil.readLine(String.format("페이지 %d/%d | 이전(p) 다음(n) 뒤로(0): ", page, totalPages == 0 ? 1 : totalPages));
    }

    public void showError(String msg) {
        System.out.println("[오류] " + msg);
    }
}
