package com.example.sampleordersystem.util;

import java.util.List;

public class TablePrinter {

    public static void print(List<String> headers, List<List<String>> rows) {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            widths[i] = displayWidth(headers.get(i));
        }
        for (List<String> row : rows) {
            for (int i = 0; i < row.size() && i < widths.length; i++) {
                widths[i] = Math.max(widths[i], displayWidth(row.get(i)));
            }
        }

        String separator = buildSeparator(widths);
        System.out.println(separator);
        System.out.println(buildRow(headers, widths));
        System.out.println(separator);
        if (rows.isEmpty()) {
            System.out.println("  (데이터 없음)");
        } else {
            for (List<String> row : rows) {
                System.out.println(buildRow(row, widths));
            }
        }
        System.out.println(separator);
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private static String buildRow(List<String> cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.size() ? cells.get(i) : "";
            int padding = widths[i] - displayWidth(cell);
            sb.append(" ").append(cell).append(" ".repeat(Math.max(0, padding))).append(" |");
        }
        return sb.toString();
    }

    // 한글·CJK 등 전각 문자는 터미널에서 2칸 차지 (명시적 코드포인트 범위 사용)
    private static int displayWidth(String s) {
        int width = 0;
        for (char c : s.toCharArray()) {
            width += isFullWidth(c) ? 2 : 1;
        }
        return width;
    }

    private static boolean isFullWidth(char c) {
        return (c >= 0x1100 && c <= 0x115F)   // Hangul Jamo
            || (c >= 0x2E80 && c <= 0x303E)   // CJK Radicals, Kangxi
            || (c >= 0x3041 && c <= 0x33FF)   // Hiragana, Katakana, CJK Symbols
            || (c >= 0x3400 && c <= 0x4DBF)   // CJK Extension A
            || (c >= 0x4E00 && c <= 0x9FFF)   // CJK Unified Ideographs
            || (c >= 0xA000 && c <= 0xA4CF)   // Yi
            || (c >= 0xAC00 && c <= 0xD7AF)   // Hangul Syllables
            || (c >= 0xF900 && c <= 0xFAFF)   // CJK Compatibility Ideographs
            || (c >= 0xFE10 && c <= 0xFE1F)   // Vertical Forms
            || (c >= 0xFE30 && c <= 0xFE6F)   // CJK Compatibility Forms
            || (c >= 0xFF01 && c <= 0xFF60)   // Fullwidth Latin & Punctuation
            || (c >= 0xFFE0 && c <= 0xFFE6);  // Fullwidth Signs
    }
}
