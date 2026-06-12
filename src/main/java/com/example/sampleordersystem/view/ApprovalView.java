package com.example.sampleordersystem.view;

import com.example.sampleordersystem.util.ConsoleUtil;
import com.example.sampleordersystem.util.TablePrinter;

import java.util.List;

public class ApprovalView {

    public void showSubMenu() {
        System.out.println("\n----- 주문 -----");
        System.out.println(" 1. 승인 대기 목록 조회");
        System.out.println(" 2. 전체 주문 조회");
        System.out.println(" 3. 주문 검색");
        System.out.println(" 0. 뒤로");
    }

    public String readChoice() {
        return ConsoleUtil.readLine("> ");
    }

    public void showReservedTable(List<String> headers, List<List<String>> rows, int page, int totalPages) {
        System.out.println(String.format("\n[승인 대기 주문] 페이지 %d / %d", page, totalPages == 0 ? 1 : totalPages));
        TablePrinter.print(headers, rows);
    }

    public void showAllOrdersTable(List<String> headers, List<List<String>> rows, int page, int totalPages) {
        System.out.println(String.format("\n[전체 주문 목록] 페이지 %d / %d", page, totalPages == 0 ? 1 : totalPages));
        TablePrinter.print(headers, rows);
    }

    public void showSearchResultTable(List<String> headers, List<List<String>> rows, int page, int totalPages) {
        System.out.println(String.format("\n[검색 결과] 페이지 %d / %d", page, totalPages == 0 ? 1 : totalPages));
        TablePrinter.print(headers, rows);
    }

    public String promptSearchKeyword() {
        return ConsoleUtil.readLine("검색어 (주문ID 또는 고객명): ");
    }

    public String readPageNav(int page, int totalPages) {
        return ConsoleUtil.readLine(String.format("페이지 %d/%d | 이전(p) 다음(n) 주문 선택(주문ID) 뒤로(0): ", page, totalPages == 0 ? 1 : totalPages));
    }

    public String readPageNavReadOnly(int page, int totalPages) {
        return ConsoleUtil.readLine(String.format("페이지 %d/%d | 이전(p) 다음(n) 뒤로(0): ", page, totalPages == 0 ? 1 : totalPages));
    }

    public String promptApproveOrReject() {
        System.out.println("  1. 승인    2. 거절    0. 취소");
        return ConsoleUtil.readLine("> ");
    }

    public void showStockInfo(String sampleName, int currentStock, int orderQty) {
        int afterStock = currentStock - orderQty;
        int shortage = Math.max(0, -afterStock);
        System.out.println("\n[재고 현황]");
        System.out.println("  시료: " + sampleName);
        System.out.printf("  현재 재고:    %d%n", currentStock);
        System.out.printf("  주문 수량:    %d%n", orderQty);
        System.out.printf("  변경 후 재고: %d%n", Math.max(0, afterStock));
        if (shortage > 0) {
            System.out.printf("  부족 수량:    %d  →  생산 라인 등록 예정%n", shortage);
        } else {
            System.out.println("  → 재고 충분: 즉시 CONFIRMED 전환");
        }
    }

    public boolean confirmApprove() {
        String input = ConsoleUtil.readLine("승인하시겠습니까? (y/n): ");
        return "y".equalsIgnoreCase(input);
    }

    public void showSuccess(String msg) {
        System.out.println("[완료] " + msg);
    }

    public void showError(String msg) {
        System.out.println("[오류] " + msg);
    }
}
