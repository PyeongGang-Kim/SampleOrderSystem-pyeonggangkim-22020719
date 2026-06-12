# Phase 1: 프로젝트 기반 설정 (Foundation)

## 목표
실행 가능한 프로젝트 뼈대를 구성한다. DB 연결, 스키마 초기화, 공통 유틸리티가 동작하는 상태로 완료한다.

## 구현 대상

### 빌드 설정
- `build.gradle.kts`: JUnit 5, H2 의존성 추가 확인
- `settings.gradle.kts`: 프로젝트명 `SampleOrderSystem` 확인

### DB 기반
- `db/H2ServerManager.java`: TCP 서버 기동/정지 (포트 9092), ./data/sampleorder.mv.db 영속
- `db/SchemaInitializer.java`: DDL 자동 생성 (CREATE TABLE IF NOT EXISTS)
  - samples, stocks, pending_shipment_stocks, orders, production_schedules 5개 테이블

### 유틸리티 (TDD 적용)
- `util/ConsoleUtil.java`: Scanner 기반 공통 입력 메서드 (readNonBlank, readPositiveInt)
- `util/TablePrinter.java`: 고정 포맷 콘솔 테이블 출력
- `util/Paginator.java`: PAGE_SIZE 상수, paginate(List, pageNum) 메서드
- `util/OrderIdGenerator.java`: YYYYMMDD_NNNN 형식 ID 생성, DB MAX(id) 조회 기반

### 진입점 뼈대
- `Application.java`: main 메서드, H2 서버 기동 → 스키마 초기화 → (메뉴 루프 stub)

## TDD 대상
- `Paginator`: paginate() 경계값 (첫 페이지, 마지막 페이지, 빈 리스트)
- `OrderIdGenerator`: 당일 첫 주문 → YYYYMMDD_0001, 시퀀스 증가, 날짜 변경 시 리셋

## 완료 조건
- `./gradlew test` 통과
- Application.main() 실행 시 H2 서버 기동 및 스키마 생성 확인
- Paginator, OrderIdGenerator 테스트 전부 GREEN
