# SampleOrderSystem

반도체 시료의 주문 · 생산 · 출고를 관리하는 콘솔 기반 시스템입니다.

```
    ____    ___    ____
   / ___|  / _ \  / ___|
   \___ \ | | | | \___ \
    ___) || |_| |  ___) |
   |____/  \___/  |____/

   S A M P L E  O R D E R  S Y S T E M
```

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Java 17 |
| 빌드 | Gradle 8+ |
| 데이터베이스 | H2 (TCP 서버 모드, 포트 9092) |
| 테스트 | JUnit 5 |
| 외부 프레임워크 | 없음 (순수 Java MVC) |

---

## 주요 기능

### 시료 관리
- 시료 등록 / 조회 / 검색 / 수정 / 삭제
- 진행 중인 주문이 있는 시료는 삭제 불가

### 시료 주문
- 재고 수량과 무관하게 주문 등록 가능
- 주문 ID 자동 생성 (`YYYYMMDD_NNNN` 형식)

### 주문 관리
- 승인 대기 목록 조회 (RESERVED 상태)
- 전체 주문 조회 (모든 상태)
- 주문 ID / 고객명으로 검색
- 주문 승인 / 거절 처리

### 모니터링
- 상태별 주문 수 집계 (RESERVED / PRODUCING / CONFIRMED / RELEASE)
- 시료별 재고 현황 (일반 재고 · 배송대기 재고 · 주문량 · 상태)

### 생산 라인
- 현재 생산 중인 항목 상세 표시 (FIFO 기준 첫 번째 스케줄)
- 전체 생산 대기 목록 페이징 조회
- 홈 화면에서 `생산 명령 N` 입력으로 N분 생산 시뮬레이션

### 출고
- CONFIRMED 상태 주문 목록 조회 및 출고(RELEASE) 처리

---

## 주문 상태 전환

```
[주문 등록] → RESERVED
RESERVED    → CONFIRMED   (승인 + 재고 충분)
RESERVED    → PRODUCING   (승인 + 재고 부족 → 생산 스케줄 자동 등록)
RESERVED    → REJECTED    (거절)
PRODUCING   → CONFIRMED   (생산 완료 시 자동 전환)
CONFIRMED   → RELEASE     (출고 처리)
```

---

## 생산 로직

- **분당 실제 생산량** = `prodRate × yield × 0.9` (버림)
- **생산 스케줄 목표 수량** = 주문 수량 − 현재 재고 (부족분)
- 생산 완료(목표 수량 달성) 시:
  - `Stock.subtract(주문 원래 수량)`
  - `PendingShipmentStock.add(주문 원래 수량)`
  - 주문 상태 → `CONFIRMED` 자동 전환

---

## 빌드 및 실행

### 요구사항
- JDK 17 이상
- Gradle 8+ (또는 동봉된 `gradlew` 사용)

### 실행

```bash
# Windows
./gradlew run

# 또는 JAR 빌드 후 실행
./gradlew build
java -jar build/libs/SampleOrderSystem-1.0-SNAPSHOT.jar
```

### 테스트

```bash
./gradlew test
```

### 데이터 파일 위치

실행 후 `./data/sampleorder.mv.db` 파일에 데이터가 영속 저장됩니다.

---

## 프로젝트 구조

```
src/
├── main/java/com/example/sampleordersystem/
│   ├── Application.java              # 진입점 / 수동 DI 조립
│   ├── db/
│   │   ├── H2ServerManager.java      # H2 TCP 서버 관리
│   │   └── SchemaInitializer.java    # DDL 초기화
│   ├── model/
│   │   ├── sample/Sample.java
│   │   ├── order/{Order, OrderStatus}.java
│   │   ├── inventory/{Inventory, Stock, PendingShipmentStock}.java
│   │   └── production/ProductionSchedule.java
│   ├── repository/                   # 인터페이스 + H2 JDBC 구현체
│   ├── service/                      # 비즈니스 로직
│   │   ├── SampleService.java
│   │   ├── OrderService.java
│   │   ├── ApprovalService.java
│   │   ├── InventoryService.java
│   │   ├── ProductionService.java
│   │   └── ShippingService.java
│   ├── controller/                   # 입력 처리 / 흐름 제어
│   ├── view/                         # 콘솔 출력
│   └── util/
│       ├── ConsoleUtil.java
│       ├── TablePrinter.java
│       ├── Paginator.java
│       └── OrderIdGenerator.java
└── test/                             # JUnit 5 단위 테스트
```

---

## 아키텍처 원칙

### 레이어 의존성 (단방향)
```
View → Controller → Service → Repository → Model
```
레이어를 건너뛰는 참조(예: View → Service 직접 호출)는 금지합니다.

### 코딩 규칙
- 모든 JDBC 쿼리는 `PreparedStatement` 사용 (`createStatement` 금지)
- `Sample.id`, `Order.id`는 PK이며 생성 후 변경 불가 (setter 미노출)
- 페이지당 건수는 `Paginator.PAGE_SIZE` 상수로만 참조
- 빈 `catch` 블록 금지, `SQLException`은 `RuntimeException`으로 래핑하여 re-throw

---

## 개발 방법론

TDD(Red → Green → Refactor) 사이클로 개발되었습니다.

TDD 적용 대상: `OrderStatus`, `Inventory` 계층, `ApprovalService`, `ProductionService`, `OrderIdGenerator`, `SampleService`, `Paginator`

---

## 참고 문서

| 문서 | 내용 |
|------|------|
| `PRD.md` | 요구사항, 메뉴 구성, 상태 전환 정의 |
| `ARCHITECTURE.md` | MVC 구조, DB 스키마, DI 조립 순서 |
| `CLAUDE.md` | Claude Code 개발 가이드 (페이즈, 에이전트, 코딩 규칙) |
