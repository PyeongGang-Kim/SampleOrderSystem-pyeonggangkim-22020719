package com.example.sampleordersystem.view;

import com.example.sampleordersystem.util.ConsoleUtil;
import com.example.sampleordersystem.util.TablePrinter;

import java.util.List;

public class SampleView {

    public void showSubMenu() {
        System.out.println("\n----- 시료 관리 -----");
        System.out.println(" 1. 시료 등록");
        System.out.println(" 2. 시료 조회");
        System.out.println(" 3. 시료 검색");
        System.out.println(" 4. 시료 수정");
        System.out.println(" 5. 시료 삭제");
        System.out.println(" 0. 뒤로");
    }

    public String readChoice() {
        return ConsoleUtil.readLine("> ");
    }

    public void showSampleTable(List<String> headers, List<List<String>> rows, int page, int totalPages) {
        System.out.println(String.format("\n[시료 목록] 페이지 %d / %d", page, totalPages == 0 ? 1 : totalPages));
        TablePrinter.print(headers, rows);
    }

    public String readPageNav(int page, int totalPages) {
        return ConsoleUtil.readLine(String.format("페이지 %d/%d | 이전(p) 다음(n) 뒤로(0): ", page, totalPages == 0 ? 1 : totalPages));
    }

    public String promptName() {
        return ConsoleUtil.readNonBlank("시료명: ");
    }

    public double promptProdRate() {
        while (true) {
            try {
                return Double.parseDouble(ConsoleUtil.readNonBlank("분당 생산속도: "));
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    public double promptYield() {
        while (true) {
            try {
                double v = Double.parseDouble(ConsoleUtil.readNonBlank("수율 (0~1): "));
                if (v > 0 && v <= 1) return v;
                System.out.println("0 초과 1 이하의 값을 입력해주세요.");
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    public Long promptSampleId(String label) {
        while (true) {
            try {
                return Long.parseLong(ConsoleUtil.readNonBlank(label));
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    public String promptKeyword() {
        return ConsoleUtil.readNonBlank("검색어 (ID 또는 이름): ");
    }

    public boolean confirmDelete(String sampleName) {
        String input = ConsoleUtil.readLine("'" + sampleName + "' 를 삭제하시겠습니까? (y/n): ");
        return "y".equalsIgnoreCase(input);
    }

    public void showSuccess(String msg) {
        System.out.println("[완료] " + msg);
    }

    public void showError(String msg) {
        System.out.println("[오류] " + msg);
    }
}
