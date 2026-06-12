package com.example.sampleordersystem.util;

import java.util.List;

public class TablePrinter {

    public static void print(List<String> headers, List<List<String>> rows) {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < row.size() && i < widths.length; i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
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
            sb.append(String.format(" %-" + widths[i] + "s |", cell));
        }
        return sb.toString();
    }
}
