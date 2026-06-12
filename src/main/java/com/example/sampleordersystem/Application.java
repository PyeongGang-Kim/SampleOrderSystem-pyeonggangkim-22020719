package com.example.sampleordersystem;

import com.example.sampleordersystem.db.H2ServerManager;
import com.example.sampleordersystem.db.SchemaInitializer;

import java.sql.Connection;

public class Application {

    public static void main(String[] args) {
        H2ServerManager.start();
        Runtime.getRuntime().addShutdownHook(new Thread(H2ServerManager::stop));

        try (Connection conn = H2ServerManager.getConnection()) {
            SchemaInitializer.init(conn);
            System.out.println("=== 반도체 시료 생산주문관리 시스템 ===");
            System.out.println("DB 초기화 완료. (메뉴 구현 예정)");
        } catch (Exception e) {
            throw new RuntimeException("애플리케이션 초기화 실패", e);
        }
    }
}
