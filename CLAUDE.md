# SampleOrderSystem — Claude Code 가이드

## 프로젝트 개요
반도체 시료의 주문/생산/출고를 관리하는 콘솔 기반 시스템.
Java 17, Gradle 8+, H2 DB (TCP 서버 모드), JUnit 5, 외부 프레임워크 없음.

## 핵심 문서
- `PRD.md`: 시스템 요구사항, 메뉴 구성, 주문 상태 전환 정의
- `ARCHITECTURE.md`: MVC 레이어 구조, DB 스키마, 패키지 설계, DI 조립 순서

## 개발 방법론
모든 기능 구현 전 `superpowers:test-driven-development` 스킬을 호출하여 TDD 사이클(Red → Green → Refactor)로 진행한다.
TDD 우선 적용 대상: `OrderStatus`, `Inventory` 계층, `ProductionService`, `ApprovalService`, `OrderIdGenerator`, `SampleService`, `Paginator`

## 개발 페이즈

| 페이즈 | 파일 | 핵심 내용 |
|--------|------|-----------|
| Phase 1 | `.claude/phases/phase1-foundation.md` | Gradle 설정, H2 DB 기반, 유틸리티 (TDD) |
| Phase 2 | `.claude/phases/phase2-domain-model.md` | Model 클래스 전체 (TDD 집중) |
| Phase 3 | `.claude/phases/phase3-repository.md` | Repository 인터페이스 + H2 JDBC 구현체 |
| Phase 4 | `.claude/phases/phase4-service.md` | Service 비즈니스 로직 (TDD 집중) |
| Phase 5 | `.claude/phases/phase5-presentation.md` | Controller / View / Application DI 조립 |
| Phase 6 | `.claude/phases/phase6-verification.md` | Agent 기반 통합 검증 및 마무리 |

## Agent 구성

| 에이전트 | 파일 | 역할 |
|---------|------|------|
| orchestrator | `.claude/agents/orchestrator.md` | 전체 조율, 도메인 지식, 호출 판단 기준 |
| subagent1 | `.claude/agents/subagent1-doc-consistency.md` | PRD·ARCHITECTURE·코드 정합성 검증 |
| subagent2 | `.claude/agents/subagent2-ai-action.md` | 검증 보고서 기반 자동 수정 |
| subagent3 | `.claude/agents/subagent3-test-verify.md` | 테스트 커버리지 및 품질 검증 |
| subagent4 | `.claude/agents/subagent4-compliance-verify.md` | JDBC 보안·레이어 의존성·PK 불변성 검증 |

## 아키텍처 핵심 규칙

### 레이어 의존성 (위반 금지)
- View → Controller → Service → Repository → Model 단방향
- View가 Service/Repository를 직접 참조하는 것은 금지
- Model이 다른 레이어를 참조하는 것은 금지

### 주문 상태 전환
- RESERVED → CONFIRMED: 승인 + 재고 충분
- RESERVED → PRODUCING: 승인 + 재고 부족
- RESERVED → REJECTED: 거절
- PRODUCING → CONFIRMED: 생산 완료 시 자동
- CONFIRMED → RELEASE: 출고 처리

### 재고 계층
- Inventory(abstract) → Stock(일반 재고), PendingShipmentStock(배송대기 재고)
- Stock: 생산 완료 시 add(), CONFIRMED 전환 시 subtract()
- PendingShipmentStock: CONFIRMED 전환 시 add(), RELEASE 처리 시 subtract()

### 생산량 계산
- 분당 실제 생산량 = prodRate / (yield * 0.9)
- 반복 루프 없이 나눗셈으로 한 번에 계산

## 코딩 규칙
- 모든 JDBC 쿼리는 PreparedStatement 사용 (createStatement 금지)
- Sample.id, Order.id는 PK이며 setter 노출 금지
- 페이지 건수는 Paginator.PAGE_SIZE 상수로만 참조 (매직 넘버 금지)
- 빈 catch 블록 금지, SQLException은 RuntimeException으로 래핑하여 re-throw
