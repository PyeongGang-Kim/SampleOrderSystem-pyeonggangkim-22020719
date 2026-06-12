# Phase 6: 통합 검증 및 마무리 (Integration & Verification)

## 목표
Agent 기반 자동 검증을 통해 문서 정합성, 테스트 품질, 컴플라이언스를 최종 확인하고 릴리스 가능한 상태로 완료한다.

## 실행 순서

### Step 1: 문서 정합성 검증
`subagent1-doc-consistency` 호출

확인 항목:
- PRD의 주문 상태 전환 ↔ OrderStatus.canTransitionTo() 구현 일치
- ARCHITECTURE의 패키지 구조 ↔ 실제 소스 디렉토리 구조 일치
- ARCHITECTURE의 DB 스키마 ↔ SchemaInitializer DDL 일치
- ARCHITECTURE의 생산량 공식 ↔ ProductionService 구현 일치
- PRD의 시료 삭제 제약 ↔ SampleService 구현 일치

### Step 2: 문서 갭 자동 수정
`subagent2-ai-action` 호출 (subagent1 보고서 입력)

- [CRITICAL] / [WARNING] 항목 자동 수정
- 수동 검토 필요 항목 ESCALATE 목록 확인

### Step 3: 테스트 검증
`subagent3-test-verify` 호출

확인 항목:
- OrderStatus 전환 전체 케이스 (정상 + 예외) 테스트 존재 여부
- Inventory subtract() 잔량 부족 예외 테스트 존재 여부
- ProductionService 생산량 계산 공식 테스트 존재 여부
- ApprovalService 재고 충분/부족 분기 테스트 존재 여부
- SampleService 삭제 제약 테스트 존재 여부
- OrderIdGenerator 형식 및 시퀀스 테스트 존재 여부

### Step 4: 컴플라이언스 검증
`subagent4-compliance-verify` 호출

확인 항목:
- JDBC 코드 전체 PreparedStatement 사용 여부 (createStatement 금지)
- Sample.id, Order.id setter 노출 여부 (PK 불변성)
- 레이어 의존성 위반 여부 (View → Service 직접 참조 등)
- 빈 catch 블록 또는 printStackTrace() 직접 사용 여부
- Paginator.PAGE_SIZE 상수 사용 여부 (매직 넘버 금지)

### Step 5: 페이징 건수 조정
실제 화면 출력을 확인하여 Paginator.PAGE_SIZE 값 조정

### Step 6: 최종 확인
- `./gradlew test` 전체 통과
- 모든 [BLOCKER] / [CRITICAL] 항목 해소 확인
- git 커밋 및 푸시

## 완료 조건
- subagent1 보고서: [CRITICAL] 0건
- subagent3 보고서: 핵심 비즈니스 로직 커버리지 항목 전부 O
- subagent4 보고서: [BLOCKER] 0건, [CRITICAL] 0건
- `./gradlew test` 전체 GREEN
