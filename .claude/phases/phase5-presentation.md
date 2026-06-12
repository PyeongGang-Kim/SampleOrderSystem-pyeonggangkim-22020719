# Phase 5: 프레젠테이션 계층 (Controller / View / DI 조립)

## 목표
콘솔 UI를 완성하고 Application에서 전체 객체를 조립하여 시스템이 엔드-투-엔드로 동작하는 상태로 완료한다.

## 구현 대상

### View (콘솔 입출력)
각 View는 ConsoleUtil을 통해 입력을 받고, TablePrinter로 결과를 출력한다.
View는 데이터를 직접 가공하지 않고 표시만 담당한다.

- `view/HomeView.java`: 메인 메뉴 출력, 생산 명령 입력 처리
- `view/SampleView.java`: 시료 등록/조회/검색/수정/삭제 화면
- `view/OrderView.java`: 주문 등록 화면, 주문 목록 페이징 출력
- `view/ApprovalView.java`: RESERVED 주문 목록, 승인/거절 선택, 재고 현황 표시
- `view/MonitorView.java`: 주문량 상태별 집계, 재고량 테이블 출력
- `view/ProductionView.java`: 생산 현황 목록, 대기 주문 목록 출력
- `view/ShippingView.java`: CONFIRMED 주문 목록, 출고 처리, 출고 이력 출력

### Controller
각 Controller는 View에서 입력을 받아 Service를 호출하고 결과를 View에 전달한다.
예외는 Controller에서 catch하여 View로 에러 메시지를 전달한다.

- `controller/HomeController.java`: 메인 메뉴 루프, 생산 명령 파싱 (`생산 명령 N`)
- `controller/SampleController.java`: 시료 관리 서브 메뉴 루프
- `controller/OrderController.java`: 주문 등록 흐름
- `controller/ApprovalController.java`: 승인/거절 흐름, 재고 분기 결과 출력
- `controller/MonitorController.java`: 주문량/재고량 조회
- `controller/ProductionController.java`: 생산 현황/대기 주문 조회
- `controller/ShippingController.java`: 출고 처리 흐름

### Application 최종 조립
ARCHITECTURE.md 섹션 11의 수동 DI 순서대로 조립한다.
```
H2ServerManager.start() → SchemaInitializer.init() →
Repository 5개 → Service 6개 → View 7개 → Controller 7개 →
homeCtrl.run()
```

## 페이징 적용 화면
- 시료 조회: SampleView
- 주문 목록: OrderView, ApprovalView, ShippingView
- 생산 스케쥴 목록: ProductionView
- Paginator.PAGE_SIZE 상수 사용 (매직 넘버 금지)

## 완료 조건
- Application.main() 실행 시 전체 메뉴 동작 확인
- 각 메뉴의 정상 경로(happy path) 수동 확인
  - 시료 등록 → 주문 등록 → 승인(재고 충분) → 출고
  - 시료 등록 → 주문 등록 → 승인(재고 부족) → 생산 명령 → 자동 CONFIRMED → 출고
- `./gradlew test` 통과
