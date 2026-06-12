package com.example.sampleordersystem.view;

import com.example.sampleordersystem.util.ConsoleUtil;

public class HomeView {

    public void showMenu() {
        System.out.println("\n========== 시료 생산주문관리 시스템 ==========");
        System.out.println(" 1. 시료 관리");
        System.out.println(" 2. 시료 주문");
        System.out.println(" 3. 주문");
        System.out.println(" 4. 모니터링");
        System.out.println(" 5. 생산 라인");
        System.out.println(" 6. 출고");
        System.out.println(" 0. 종료");
        System.out.println("  (생산 명령: '생산 명령 N' 입력)");
        System.out.println("============================================");
    }

    public String readInput() {
        return ConsoleUtil.readLine("> ");
    }

    public void showAdvanceResult(int minutes) {
        System.out.println("[생산 완료] " + minutes + "분 생산 처리가 완료되었습니다.");
    }

    public void showError(String msg) {
        System.out.println("[오류] " + msg);
    }
}
