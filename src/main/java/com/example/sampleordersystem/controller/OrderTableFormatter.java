package com.example.sampleordersystem.controller;

import com.example.sampleordersystem.model.order.Order;
import com.example.sampleordersystem.service.SampleService;

import java.util.ArrayList;
import java.util.List;

/** Order 목록 테이블의 헤더·행 구성 로직을 공유하는 패키지-전용 유틸. */
class OrderTableFormatter {

    static final List<String> DEFAULT_HEADERS =
            List.of("주문 ID", "시료명", "고객명", "수량", "등록일시");

    static final List<String> FULL_HEADERS =
            List.of("주문 ID", "시료명", "고객명", "수량", "상태", "등록일시");

    static List<List<String>> buildRows(List<Order> orders, SampleService sampleSvc) {
        List<List<String>> rows = new ArrayList<>();
        for (Order o : orders) {
            rows.add(List.of(
                    o.getId(),
                    sampleSvc.resolveSampleName(o.getSampleId()),
                    o.getCustomerName(),
                    String.valueOf(o.getQuantity()),
                    o.getCreatedAt().toString()
            ));
        }
        return rows;
    }

    static List<List<String>> buildRowsWithStatus(List<Order> orders, SampleService sampleSvc) {
        List<List<String>> rows = new ArrayList<>();
        for (Order o : orders) {
            rows.add(List.of(
                    o.getId(),
                    sampleSvc.resolveSampleName(o.getSampleId()),
                    o.getCustomerName(),
                    String.valueOf(o.getQuantity()),
                    o.getStatus().name(),
                    o.getCreatedAt().toString()
            ));
        }
        return rows;
    }
}
