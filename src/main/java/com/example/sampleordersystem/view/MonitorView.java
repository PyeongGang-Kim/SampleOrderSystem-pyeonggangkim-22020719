package com.example.sampleordersystem.view;

import com.example.sampleordersystem.util.ConsoleUtil;
import com.example.sampleordersystem.util.TablePrinter;

import java.util.List;

public class MonitorView {

    public void showSubMenu() {
        System.out.println();
        System.out.println("  ┌─ 모니터링 ──────────────────────────────────");
        System.out.println("  │  1. 주문량 확인    2. 재고량 확인    0. 뒤로");
        System.out.println("  └────────────────────────────────────────────");
    }

    public String readChoice() {
        return ConsoleUtil.readLine("> ");
    }

    public void showOrderSummary(int reserved, int confirmed, int producing, int release) {
        System.out.println();
        System.out.println("  ╔══ 주문량 현황 ═════════════════════════════");
        System.out.printf ("  ║  RESERVED  (승인 대기) :  %d건%n", reserved);
        System.out.printf ("  ║  PRODUCING (생산 중)   :  %d건%n", producing);
        System.out.printf ("  ║  CONFIRMED (출고 대기) :  %d건%n", confirmed);
        System.out.printf ("  ║  RELEASE   (출고 완료) :  %d건%n", release);
        System.out.println("  ╚════════════════════════════════════════════");
    }

    public void showInventoryTable(List<String> headers, List<List<String>> rows) {
        System.out.println("\n[재고량 현황]");
        TablePrinter.print(headers, rows);
    }

    public void showError(String msg) {
        System.out.println("[오류] " + msg);
    }
}
