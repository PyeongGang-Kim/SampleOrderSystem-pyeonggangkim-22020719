package com.example.sampleordersystem.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginatorTest {

    private final List<Integer> list10 = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    @Test
    void 빈_리스트를_페이징하면_빈_리스트를_반환한다() {
        List<Integer> result = Paginator.paginate(Collections.emptyList(), 1);
        assertTrue(result.isEmpty());
    }

    @Test
    void 첫_번째_페이지는_첫_PAGE_SIZE개를_반환한다() {
        List<Integer> result = Paginator.paginate(list10, 1);
        assertEquals(Paginator.PAGE_SIZE, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    void 마지막_페이지는_나머지_항목을_반환한다() {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        int lastPage = (int) Math.ceil((double) list.size() / Paginator.PAGE_SIZE);
        List<Integer> result = Paginator.paginate(list, lastPage);
        assertFalse(result.isEmpty());
        assertEquals(11, result.get(result.size() - 1));
    }

    @Test
    void 범위를_초과한_페이지는_빈_리스트를_반환한다() {
        List<Integer> result = Paginator.paginate(list10, 999);
        assertTrue(result.isEmpty());
    }

    @Test
    void 전체_페이지수를_계산한다() {
        assertEquals(1, Paginator.totalPages(list10));
        assertEquals(2, Paginator.totalPages(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)));
        assertEquals(0, Paginator.totalPages(Collections.emptyList()));
    }
}
