package com.example.sampleordersystem;

import com.example.sampleordersystem.controller.*;
import com.example.sampleordersystem.db.H2ServerManager;
import com.example.sampleordersystem.db.SchemaInitializer;
import com.example.sampleordersystem.repository.impl.*;
import com.example.sampleordersystem.service.*;
import com.example.sampleordersystem.view.*;

import java.sql.Connection;

public class Application {

    public static void main(String[] args) {
        H2ServerManager.start();
        Runtime.getRuntime().addShutdownHook(new Thread(H2ServerManager::stop));

        Connection conn = H2ServerManager.getConnection();
        SchemaInitializer.init(conn);

        // Repository
        H2SampleRepository sampleRepo = new H2SampleRepository(conn);
        H2OrderRepository orderRepo = new H2OrderRepository(conn);
        H2StockRepository stockRepo = new H2StockRepository(conn);
        H2PendingShipmentStockRepository pendingRepo = new H2PendingShipmentStockRepository(conn);
        H2ProductionScheduleRepository prodRepo = new H2ProductionScheduleRepository(conn);

        // Service
        SampleService sampleSvc = new SampleService(sampleRepo, orderRepo, stockRepo, pendingRepo);
        InventoryService inventorySvc = new InventoryService(stockRepo, pendingRepo, orderRepo);
        ProductionService productionSvc = new ProductionService(prodRepo, stockRepo, pendingRepo, orderRepo, sampleRepo);
        OrderService orderSvc = new OrderService(orderRepo, sampleRepo);
        ApprovalService approvalSvc = new ApprovalService(orderRepo, stockRepo, pendingRepo, prodRepo);
        ShippingService shippingSvc = new ShippingService(orderRepo, pendingRepo);

        // View
        HomeView homeView = new HomeView();
        SampleView sampleView = new SampleView();
        OrderView orderView = new OrderView();
        ApprovalView approvalView = new ApprovalView();
        MonitorView monitorView = new MonitorView();
        ProductionView productionView = new ProductionView();
        ShippingView shippingView = new ShippingView();

        // Controller
        SampleController sampleCtrl = new SampleController(sampleSvc, inventorySvc, sampleView);
        OrderController orderCtrl = new OrderController(orderSvc, sampleSvc, orderView);
        ApprovalController approvalCtrl = new ApprovalController(approvalSvc, inventorySvc, orderSvc, sampleSvc, approvalView);
        MonitorController monitorCtrl = new MonitorController(inventorySvc, sampleSvc, orderSvc, monitorView);
        ProductionController productionCtrl = new ProductionController(productionSvc, sampleSvc, productionView);
        ShippingController shippingCtrl = new ShippingController(shippingSvc, orderSvc, sampleSvc, shippingView);
        HomeController homeCtrl = new HomeController(productionSvc, homeView,
                sampleCtrl, orderCtrl, approvalCtrl, monitorCtrl, productionCtrl, shippingCtrl);

        homeCtrl.run();
    }
}
