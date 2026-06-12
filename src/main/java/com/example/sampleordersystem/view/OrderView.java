package com.example.sampleordersystem.view;

import com.example.sampleordersystem.util.ConsoleUtil;
import com.example.sampleordersystem.util.TablePrinter;

import java.util.List;

public class OrderView {

    public Long promptSampleId() {
        while (true) {
            try {
                return Long.parseLong(ConsoleUtil.readNonBlank("시료 ID: "));
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    public String promptCustomerName() {
        return ConsoleUtil.readNonBlank("고객명: ");
    }

    public int promptQuantity() {
        return ConsoleUtil.readPositiveInt("주문 수량: ");
    }

    public boolean confirmOrder(String sampleName, String customerName, int quantity) {
        System.out.println();
        System.out.println("  ╔══ 주문 확인 ═══════════════════════════════");
        System.out.println("  ║  시료  :  " + sampleName);
        System.out.println("  ║  고객  :  " + customerName);
        System.out.println("  ║  수량  :  " + quantity);
        System.out.println("  ╚════════════════════════════════════════════");
        String input = ConsoleUtil.readLine("  주문을 확정하시겠습니까? (y/n): ");
        return "y".equalsIgnoreCase(input);
    }

    public void showOrderTable(List<String> headers, List<List<String>> rows, int page, int totalPages) {
        System.out.println(String.format("\n[주문 목록] 페이지 %d / %d", page, totalPages == 0 ? 1 : totalPages));
        TablePrinter.print(headers, rows);
    }

    public String readPageNav(int page, int totalPages) {
        return ConsoleUtil.readLine(String.format("페이지 %d/%d | 이전(p) 다음(n) 뒤로(0): ", page, totalPages == 0 ? 1 : totalPages));
    }

    public void showSuccess(String msg) {
        System.out.println("  [+] " + msg);
    }

    public void showError(String msg) {
        System.out.println("  [!] " + msg);
    }
}
