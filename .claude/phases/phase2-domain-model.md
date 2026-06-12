# Phase 2: 도메인 모델 (Domain Model)

## 목표
핵심 비즈니스 규칙이 모델 계층에 캡슐화된 상태로 완료한다. 이 페이즈의 모든 클래스는 TDD로 구현한다.

## 구현 대상

### 시료
- `model/sample/Sample.java`
  - id: Long (PK, final, 변경 불가)
  - name: String
  - prodRate: double (분당 생산량)
  - yield: double (수율)

### 주문
- `model/order/OrderStatus.java` (enum)
  - 값: RESERVED, CONFIRMED, PRODUCING, RELEASE, REJECTED
  - `canTransitionTo(OrderStatus next): boolean` — 허용 전이만 true 반환
  - 허용 전이: RESERVED→{CONFIRMED,PRODUCING,REJECTED}, PRODUCING→CONFIRMED, CONFIRMED→RELEASE
- `model/order/Order.java`
  - id: String (PK, final, 변경 불가)
  - sampleId: Long
  - customerName: String
  - quantity: int
  - status: OrderStatus
  - createdAt: LocalDateTime
  - `changeStatus(OrderStatus next)`: canTransitionTo() 검사 후 전환, 실패 시 예외

### 재고
- `model/inventory/Inventory.java` (abstract)
  - sampleId: Long
  - quantity: int
  - `add(int amount): void` — amount > 0 검증
  - `subtract(int amount): void` — 잔량 부족 시 예외
- `model/inventory/Stock.java extends Inventory`
- `model/inventory/PendingShipmentStock.java extends Inventory`

### 생산 스케쥴
- `model/production/ProductionSchedule.java`
  - id: Long (PK)
  - orderId: String
  - sampleId: Long
  - targetQuantity: int
  - producedQuantity: int
  - createdAt: LocalDateTime
  - `isComplete(): boolean` — producedQuantity >= targetQuantity
  - `addProduced(int amount): void` — producedQuantity 누적

## TDD 대상 및 핵심 케이스

### OrderStatusTest
- RESERVED → CONFIRMED: true
- RESERVED → PRODUCING: true
- RESERVED → REJECTED: true
- RESERVED → RELEASE: false (예외)
- PRODUCING → CONFIRMED: true
- PRODUCING → RESERVED: false (예외)
- CONFIRMED → RELEASE: true
- CONFIRMED → RESERVED: false (예외)
- RELEASE → 모든 전환: false (예외)
- REJECTED → 모든 전환: false (예외)

### InventoryTest (Stock, PendingShipmentStock 공통)
- add() 후 quantity 증가 확인
- subtract() 후 quantity 감소 확인
- subtract() 잔량 초과 시 예외 발생
- add() 음수 입력 시 예외 발생

### ProductionScheduleTest
- isComplete(): producedQuantity < targetQuantity → false
- isComplete(): producedQuantity == targetQuantity → true
- addProduced() 누적 확인

## 완료 조건
- `./gradlew test` 통과
- Model 클래스 전체 테스트 GREEN
- Model 클래스가 다른 레이어(Service, Repository 등)를 import하지 않음
