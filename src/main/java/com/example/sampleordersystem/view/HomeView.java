package com.example.sampleordersystem.view;

import com.example.sampleordersystem.util.ConsoleUtil;

public class HomeView {

    public void showMenu() {
        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("      ____    ___    ____");
        System.out.println("     / ___|  / _ \\  / ___|");
        System.out.println("     \\___ \\ | | | | \\___ \\");
        System.out.println("      ___) || |_| |  ___) |");
        System.out.println("     |____/  \\___/  |____/");
        System.out.println();
        System.out.println("     S A M P L E  O R D E R  S Y S T E M");
        System.out.println("     반도체 시료 생산주문관리 시스템");
        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("   [ 1 ] 시료 관리          [ 2 ] 시료 주문");
        System.out.println("   [ 3 ] 주문               [ 4 ] 모니터링");
        System.out.println("   [ 5 ] 생산 라인          [ 6 ] 출고");
        System.out.println();
        System.out.println("  ──────────────────────────────────────────────────────");
        System.out.println("   [ 0 ] 종료          >>  생산 명령: '생산 명령 N'");
        System.out.println("  ══════════════════════════════════════════════════════");
    }

    public String readInput() {
        return ConsoleUtil.readLine("\n  >> ");
    }

    public void showAdvanceResult(int minutes) {
        System.out.println();
        System.out.println("  ──────────────────────────────────────────────────────");
        System.out.printf("   ★ 생산 완료  %d분 생산 처리가 완료되었습니다.%n", minutes);
        System.out.println("  ──────────────────────────────────────────────────────");
    }

    public void showError(String msg) {
        System.out.println("  [!] " + msg);
    }
}
