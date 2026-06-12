package com.example.sampleordersystem.util;

import java.util.Collections;
import java.util.List;

public class Paginator {

    public static final int PAGE_SIZE = 10;

    public static <T> List<T> paginate(List<T> list, int page) {
        if (list.isEmpty()) return Collections.emptyList();
        int fromIndex = (page - 1) * PAGE_SIZE;
        if (fromIndex >= list.size()) return Collections.emptyList();
        int toIndex = Math.min(fromIndex + PAGE_SIZE, list.size());
        return list.subList(fromIndex, toIndex);
    }

    public static int totalPages(List<?> list) {
        if (list.isEmpty()) return 0;
        return (int) Math.ceil((double) list.size() / PAGE_SIZE);
    }
}
