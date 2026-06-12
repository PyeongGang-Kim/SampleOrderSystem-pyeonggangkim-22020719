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

    public void showScheduleTable(List<String> headers, List<List<String>> rows, int page, int totalPages) {
        System.out.println(String.format("\n[생산 현황] 페이지 %d / %d", page, totalPages == 0 ? 1 : totalPages));
        TablePrinter.print(headers, rows);
    }

    public void showPendingOrderTable(List<String> headers, List<List<String>> rows) {
        System.out.println("\n[생산 대기 주문]");
        TablePrinter.print(headers, rows);
    }

    public String readPageNav(int page, int totalPages) {
        return ConsoleUtil.readLine(String.format("페이지 %d/%d | 이전(p) 다음(n) 뒤로(0): ", page, totalPages == 0 ? 1 : totalPages));
    }

    public void showError(String msg) {
        System.out.println("[오류] " + msg);
    }
}
