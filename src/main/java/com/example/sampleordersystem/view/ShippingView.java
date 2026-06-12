package com.example.sampleordersystem.view;

import com.example.sampleordersystem.util.ConsoleUtil;
import com.example.sampleordersystem.util.TablePrinter;

import java.util.List;

public class ShippingView {

    public void showConfirmedTable(List<String> headers, List<List<String>> rows, int page, int totalPages) {
        System.out.println(String.format("\n[출고 대기 주문] 페이지 %d / %d", page, totalPages == 0 ? 1 : totalPages));
        TablePrinter.print(headers, rows);
    }

    public String readPageNav(int page, int totalPages) {
        return ConsoleUtil.readLine(String.format("페이지 %d/%d | 이전(p) 다음(n) 주문 선택(주문ID) 뒤로(0): ", page, totalPages == 0 ? 1 : totalPages));
    }

    public void showShippingResult(String orderId, String sampleName, String customerName, int quantity) {
        System.out.println("\n[출고 완료]");
        System.out.println("  주문 ID: " + orderId);
        System.out.println("  시료:    " + sampleName);
        System.out.println("  고객:    " + customerName);
        System.out.println("  수량:    " + quantity);
    }

    public void showSuccess(String msg) {
        System.out.println("[완료] " + msg);
    }

    public void showError(String msg) {
        System.out.println("[오류] " + msg);
    }
}
