package com.example.sampleordersystem.db;

import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2ServerManager {

    private static final String JDBC_URL = "jdbc:h2:tcp://localhost:9092/./data/sampleorder";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static Server server;

    public static void start() {
        try {
            new java.io.File("data").mkdirs();
            server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-ifNotExists").start();
        } catch (SQLException e) {
            throw new RuntimeException("H2 서버 기동 실패", e);
        }
    }

    public static void stop() {
        if (server != null && server.isRunning(false)) {
            server.stop();
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("DB 연결 실패", e);
        }
    }
}
