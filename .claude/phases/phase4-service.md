# Phase 4: Service 계층 (Business Logic)

## 목표
핵심 비즈니스 로직을 Service 계층에 구현한다. 이 페이즈가 시스템의 핵심이며 TDD를 가장 집중적으로 적용한다.

## 구현 대상

### SampleService
- `createSample(String name, double prodRate, double yield): Sample`
- `getAllSamples(): List<Sample>`
- `findById(Long id): Optional<Sample>`
- `searchSamples(String keyword): List<Sample>`
- `updateSample(Long id, String name, double prodRate, double yield): Sample`
- `deleteSample(Long id): void` — RESERVED/PRODUCING/CONFIRMED 상태 주문 존재 시 예외

### OrderService
- `createOrder(Long sampleId, String customerName, int quantity): Order` — RESERVED 상태로 생성
- `getReservedOrders(): List<Order>`
- `getConfirmedOrders(): List<Order>`
- `getOrdersByStatus(OrderStatus status): List<Order>`

### ApprovalService
- `approve(String orderId): void`
  - Stock.quantity >= order.quantity → CONFIRMED 전환 + Stock.subtract() + PendingShipmentStock.add()
  - Stock.quantity < order.quantity → ProductionSchedule 등록 + PRODUCING 전환
- `reject(String orderId): void` — REJECTED 전환

### InventoryService
- `getStockStatus(Long sampleId): StockStatus` — 고갈/부족/여유 판단
  - 고갈: stock.quantity == 0
  - 부족: stock.quantity > 0 && stock.quantity < 주문된 수량 합계
  - 여유: stock.quantity >= 주문된 수량 합계
- `getInventorySummary(): List<InventorySummary>` — 모니터링 화면용

### ProductionService
- `advance(int minutes): void` — 생산 명령 처리
  - 생산 스케쥴 FIFO 조회
  - 각 스케쥴: 분당 실제 생산량 = prodRate / (yield * 0.9)
  - 총 생산량 = 분당 실제 생산량 * minutes (반복 없이 한 번에 계산)
  - Stock.add(생산량), schedule.addProduced(생산량)
  - isComplete() == true: 스케쥴 삭제 → Stock.subtract(targetQuantity) → PendingShipmentStock.add(targetQuantity) → Order CONFIRMED 자동 전환
- `getProductionSchedules(): List<ProductionSchedule>`

### ShippingService
- `release(String orderId): void`
  - Order CONFIRMED → RELEASE 전환
  - PendingShipmentStock.subtract(order.quantity)

## TDD 핵심 케이스

### ApprovalServiceTest
- 재고 충분 시: Order → CONFIRMED, Stock 차감, PendingShipmentStock 증가
- 재고 부족 시: Order → PRODUCING, ProductionSchedule 등록, Stock 변동 없음
- 이미 CONFIRMED인 주문 승인 시도: 예외 발생

### ProductionServiceTest
- 생산량 계산: prodRate=10.0, yield=1.0, minutes=5 → 총 생산량 = 10/(1*0.9)*5 = 55.5...
- 생산 완료 시 자동 전환: Order PRODUCING → CONFIRMED
- 생산 완료 시 Stock 차감, PendingShipmentStock 증가
- 최적화 검증: 대량 생산에도 단순 곱셈으로 처리

### SampleServiceTest
- 진행 중 주문(RESERVED) 있는 시료 삭제 시도: 예외 발생
- 진행 중 주문(PRODUCING) 있는 시료 삭제 시도: 예외 발생
- 진행 중 주문 없는 시료 삭제: 정상 처리

### InventoryServiceTest
- 재고 0: 고갈
- 재고 > 0, 재고 < 주문 합계: 부족
- 재고 >= 주문 합계: 여유

## 완료 조건
- `./gradlew test` 통과
- Service 계층 전체 테스트 GREEN
- Service가 Controller/View를 import하지 않음
