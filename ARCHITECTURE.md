# SampleOrderSystem 아키텍처 설계

## 0. 개발 방법론

### TDD (Test-Driven Development)
모든 기능 구현은 `superpowers:test-driven-development` 스킬을 적용한 TDD 사이클로 진행한다.

```
Red   → 실패하는 테스트 먼저 작성
Green → 테스트를 통과하는 최소한의 구현 작성
Refactor → 동작을 유지하면서 코드 개선
```

TDD 적용 원칙:
- 구현 코드보다 테스트 코드를 먼저 작성한다.
- 테스트는 `src/test/java/` 하위에 구현 클래스와 동일한 패키지 구조로 작성한다.
- 테스트 클래스명은 `{구현클래스명}Test` 규칙을 따른다.
- 각 테스트 메서드는 하나의 동작만 검증한다.
- 비즈니스 로직을 가진 Model, Service 클래스를 우선적으로 TDD로 구현하고, Repository는 인터페이스 기반으로 테스트한다.

TDD 우선 적용 대상 (비즈니스 로직 집중):
- `OrderStatus`: 상태 전환 허용/거부 규칙
- `Inventory`, `Stock`, `PendingShipmentStock`: 수량 증감 및 잔량 부족 예외
- `ProductionService`: 생산량 계산 공식, 생산 완료 자동 전환
- `ApprovalService`: 재고 충분/부족 분기 로직
- `OrderIdGenerator`: ID 형식 및 시퀀스 생성
- `SampleService`: 삭제 제약 조건 검증
- `Paginator`: 페이지 분할 로직

---

## 1. 기술 스택 및 참조 프로젝트 활용 방침

- 언어: Java 17
- 빌드: Gradle 8+
- DB: H2 (TCP 서버 모드, 포트 9092), 영속 파일: ./data/sampleorder.mv.db
- 외부 프레임워크 없음, 순수 Java
- 테스트: JUnit 5

| 기능 | 참조 프로젝트 | 활용 내용 |
|------|-------------|-----------|
| 전체 MVC 구조 | ConsoleMVC | Controller → Service → Repository → Model 계층 분리, 수동 DI |
| 상태 전이 | ConsoleMVC | OrderStatus.canTransitionTo() 패턴 |
| CLI 입력 파싱 | DataMonitor | CommandParser 토큰 분할 → switch 라우팅 패턴 |
| 콘솔 테이블 출력 | DataMonitor | TablePrinter 고정 포맷 출력 패턴 |
| DB 연동 | DataPersistence | H2ServerManager, SchemaInitializer, JDBC PreparedStatement |
| 더미 데이터 | DummyDataGenerator | EntityGenerator, GeneratorRegistry 패턴 |

---

## 2. 레이어 구조 및 의존성 규칙

```
View → Controller → Service → Repository → Model
                 ↘ View
```

- View는 Controller만 호출한다.
- Controller는 Service와 View에만 의존한다.
- Service는 Repository에만 의존한다.
- Repository는 Model에만 의존한다.
- Model은 어떤 레이어도 참조하지 않는다.
- 레이어를 건너뛰는 참조는 금지한다 (View → Service 직접 호출 등).

---

## 3. 패키지 구조

```
src/main/java/com/example/sampleordersystem/
├── Application.java
├── db/
│   ├── H2ServerManager.java
│   └── SchemaInitializer.java
├── model/
│   ├── sample/
│   │   └── Sample.java
│   ├── order/
│   │   ├── Order.java
│   │   └── OrderStatus.java
│   ├── inventory/
│   │   ├── Inventory.java
│   │   ├── Stock.java
│   │   └── PendingShipmentStock.java
│   └── production/
│       └── ProductionSchedule.java
├── repository/
│   ├── SampleRepository.java
│   ├── OrderRepository.java
│   ├── StockRepository.java
│   ├── PendingShipmentStockRepository.java
│   ├── ProductionScheduleRepository.java
│   └── impl/
│       ├── H2SampleRepository.java
│       ├── H2OrderRepository.java
│       ├── H2StockRepository.java
│       ├── H2PendingShipmentStockRepository.java
│       └── H2ProductionScheduleRepository.java
├── service/
│   ├── SampleService.java
│   ├── OrderService.java
│   ├── ApprovalService.java
│   ├── InventoryService.java
│   ├── ProductionService.java
│   └── ShippingService.java
├── controller/
│   ├── HomeController.java
│   ├── SampleController.java
│   ├── OrderController.java
│   ├── ApprovalController.java
│   ├── MonitorController.java
│   ├── ProductionController.java
│   └── ShippingController.java
├── view/
│   ├── HomeView.java
│   ├── SampleView.java
│   ├── OrderView.java
│   ├── ApprovalView.java
│   ├── MonitorView.java
│   ├── ProductionView.java
│   └── ShippingView.java
└── util/
    ├── ConsoleUtil.java
    ├── TablePrinter.java
    ├── Paginator.java
    └── OrderIdGenerator.java
```

---

## 4. DB 스키마

```sql
CREATE TABLE samples (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    prod_rate DOUBLE       NOT NULL,
    yield     DOUBLE       NOT NULL
);

CREATE TABLE stocks (
    sample_id BIGINT PRIMARY KEY,
    quantity  INT NOT NULL DEFAULT 0,
    FOREIGN KEY (sample_id) REFERENCES samples(id)
);

CREATE TABLE pending_shipment_stocks (
    sample_id BIGINT PRIMARY KEY,
    quantity  INT NOT NULL DEFAULT 0,
    FOREIGN KEY (sample_id) REFERENCES samples(id)
);

CREATE TABLE orders (
    id            VARCHAR(50) PRIMARY KEY,   -- YYYYMMDD_NNNN
    sample_id     BIGINT      NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    quantity      INT         NOT NULL,
    status        VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sample_id) REFERENCES samples(id)
);

CREATE TABLE production_schedules (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id          VARCHAR(50) NOT NULL,
    sample_id         BIGINT      NOT NULL,
    target_quantity   INT         NOT NULL,
    produced_quantity INT         NOT NULL DEFAULT 0,
    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id)   REFERENCES orders(id),
    FOREIGN KEY (sample_id)  REFERENCES samples(id)
);
```

---

## 5. 모델 상세

### Sample
- id: Long (PK, AUTO_INCREMENT, 변경 불가)
- name: String
- prodRate: double (분당 생산량)
- yield: double (수율)

### OrderStatus (enum)
- 값: RESERVED, CONFIRMED, PRODUCING, RELEASE, REJECTED
- canTransitionTo(next) 메서드로 전이 유효성 검사
- 허용 전이 목록:
  - RESERVED → CONFIRMED, PRODUCING, REJECTED
  - PRODUCING → CONFIRMED
  - CONFIRMED → RELEASE
  - RELEASE, REJECTED → (전이 불가)

### Order
- id: String (PK, YYYYMMDD_NNNN, 변경 불가)
- sampleId: Long
- customerName: String
- quantity: int
- status: OrderStatus
- createdAt: LocalDateTime

### Inventory (abstract)
- sampleId: Long
- quantity: int
- add(int amount): void
- subtract(int amount): void — 잔량 부족 시 예외

### Stock extends Inventory
- 생산 완료 시 add(), CONFIRMED 전환 시 subtract()

### PendingShipmentStock extends Inventory
- CONFIRMED 전환 시 add(), RELEASE 처리 시 subtract()

### ProductionSchedule
- id: Long (PK, AUTO_INCREMENT)
- orderId: String
- sampleId: Long
- targetQuantity: int
- producedQuantity: int
- createdAt: LocalDateTime
- isComplete(): boolean — producedQuantity >= targetQuantity

---

## 6. 서비스 책임 분리

| 서비스 | 주요 책임 |
|--------|-----------|
| SampleService | 시료 CRUD, 삭제 시 진행 중 주문 존재 여부 검증 |
| OrderService | 주문 등록(RESERVED 생성), 주문 조회 |
| ApprovalService | 주문 승인/거절, 재고 충분 여부 판단, PRODUCING 전환 시 생산 스케쥴 등록 |
| InventoryService | Stock·PendingShipmentStock 수량 조회/변경, 재고 상태(고갈/부족/여유) 판단 |
| ProductionService | 생산 명령 처리, 생산량 계산, 생산 완료 시 CONFIRMED 자동 전환 트리거 |
| ShippingService | CONFIRMED → RELEASE 전환, PendingShipmentStock 차감 |

---

## 7. 생산 명령 처리 흐름

생산 명령 입력: `생산 명령 N` (N분)

1. ProductionService.advance(minutes) 호출
2. 현재 생산 스케쥴을 FIFO 순으로 조회 (created_at ASC)
3. 각 스케쥴에 대해:
   - 분당 실제 생산량 = prodRate * yield * 0.9
   - 이번 명령으로 생산 가능 총량 = 분당 실제 생산량 * minutes
   - 최적화: 반복 없이 한 번에 생산량 계산 (나눗셈으로 필요 분 계산)
   - Stock에 생산량 add()
   - producedQuantity 업데이트
4. isComplete() == true인 스케쥴:
   - 스케쥴 제거
   - Stock.subtract(주문 원래 수량)
   - PendingShipmentStock.add(주문 원래 수량)
   - Order 상태 CONFIRMED 자동 전환

---

## 7-1. 생산라인 화면 구성

### 생산 현황 표기
- `ProductionService.getSchedules()`의 첫 번째 항목(FIFO 기준 현재 생산 중)만 상세 표시
- 표시 항목: 스케줄 ID, 주문ID, 시료명, 목표 수량(부족분), 현재 생산량, 잔여 수량
- 스케줄이 없으면 "현재 생산 중인 항목이 없습니다." 출력

### 대기 주문 확인
- `ProductionService.getSchedules()` 전체 목록을 FIFO 순으로 페이징 출력
- 표시 항목: 스케줄 ID, 주문ID, 시료명, 목표 수량(부족분), 현재 생산량, 잔여 수량
- 기존 "생산 현황 표기"에서 보여주던 전체 스케줄 목록을 이 화면에서 담당

---

## 8. 주문 승인 처리 흐름

1. ApprovalService.approve(orderId) 호출
2. Stock.quantity >= order.quantity 여부 확인
3. 재고 충분: Stock.subtract(quantity) → PendingShipmentStock.add(quantity) → Order 상태 CONFIRMED
4. 재고 부족: ProductionSchedule 등록 (targetQuantity = order.quantity - stock.quantity, 부족분) → Order 상태 PRODUCING

---

## 9. 페이징

- 페이지당 건수는 `Paginator.PAGE_SIZE` 상수로 관리 (기본값 10, 향후 화면 구현 후 조정)
- Paginator.paginate(List, pageNum) → 해당 페이지 subList 반환
- 페이징 적용 화면: 시료 조회, 주문 목록, 생산 스케쥴 목록, 출고 대상 목록

---

## 10. ID 생성 규칙

### 시료 ID
- DB AUTO_INCREMENT, 별도 로직 불필요

### 주문 ID
- 형식: YYYYMMDD_NNNN (날짜별 시퀀스, 4자리 zero-padding)
- OrderIdGenerator.generate(date, connection):
  - 해당 날짜 prefix로 시작하는 orders를 MAX(id) 조회
  - 마지막 시퀀스 + 1로 생성
  - 당일 첫 주문이면 YYYYMMDD_0001

---

## 11. Application 조립 순서 (수동 DI)

```
H2ServerManager.start()
SchemaInitializer.init(connection)

// Repository
H2SampleRepository sampleRepo = new H2SampleRepository(connection)
H2OrderRepository orderRepo = new H2OrderRepository(connection)
H2StockRepository stockRepo = new H2StockRepository(connection)
H2PendingShipmentStockRepository pendingRepo = new H2PendingShipmentStockRepository(connection)
H2ProductionScheduleRepository prodRepo = new H2ProductionScheduleRepository(connection)

// Service
SampleService sampleSvc = new SampleService(sampleRepo, orderRepo)
InventoryService inventorySvc = new InventoryService(stockRepo, pendingRepo, orderRepo)
ProductionService productionSvc = new ProductionService(prodRepo, stockRepo, pendingRepo, orderRepo)
OrderService orderSvc = new OrderService(orderRepo, sampleRepo)
ApprovalService approvalSvc = new ApprovalService(orderRepo, stockRepo, prodRepo)
ShippingService shippingSvc = new ShippingService(orderRepo, pendingRepo)

// View
SampleView sampleView = new SampleView()
OrderView orderView = new OrderView()
ApprovalView approvalView = new ApprovalView()
MonitorView monitorView = new MonitorView()
ProductionView productionView = new ProductionView()
ShippingView shippingView = new ShippingView()
HomeView homeView = new HomeView()

// Controller
SampleController sampleCtrl = new SampleController(sampleSvc, sampleView)
OrderController orderCtrl = new OrderController(orderSvc, orderView)
ApprovalController approvalCtrl = new ApprovalController(approvalSvc, approvalView)
MonitorController monitorCtrl = new MonitorController(inventorySvc, orderSvc, monitorView)
ProductionController prodCtrl = new ProductionController(productionSvc, productionView)
ShippingController shippingCtrl = new ShippingController(shippingSvc, shippingView)
HomeController homeCtrl = new HomeController(productionSvc, homeView,
    sampleCtrl, orderCtrl, approvalCtrl, monitorCtrl, prodCtrl, shippingCtrl)

homeCtrl.run()  // 메인 루프 진입
```
