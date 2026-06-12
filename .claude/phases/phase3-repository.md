# Phase 3: Repository 계층 (Data Access Layer)

## 목표
DB 접근 로직을 인터페이스로 추상화하고 H2 JDBC 구현체를 작성한다. 모든 SQL은 PreparedStatement를 사용한다.

## 구현 대상

### Repository 인터페이스
- `repository/SampleRepository.java`
  - save(Sample), findById(Long), findAll(), update(Sample), deleteById(Long)
- `repository/OrderRepository.java`
  - save(Order), findById(String), findAll(), findByStatus(OrderStatus), updateStatus(String id, OrderStatus), countByDatePrefix(String datePrefix)
- `repository/StockRepository.java`
  - save(Stock), findBySampleId(Long), update(Stock)
- `repository/PendingShipmentStockRepository.java`
  - save(PendingShipmentStock), findBySampleId(Long), update(PendingShipmentStock)
- `repository/ProductionScheduleRepository.java`
  - save(ProductionSchedule), findById(Long), findAllOrderByCreatedAt(), update(ProductionSchedule), deleteById(Long)

### H2 Repository 구현체
- `repository/impl/H2SampleRepository.java`
- `repository/impl/H2OrderRepository.java`
- `repository/impl/H2StockRepository.java`
- `repository/impl/H2PendingShipmentStockRepository.java`
- `repository/impl/H2ProductionScheduleRepository.java`

### 구현 공통 원칙
- 생성자에서 `Connection`을 주입받는다.
- 모든 쿼리는 `PreparedStatement`를 사용한다 (`Statement` 직접 사용 금지).
- `SQLException`은 catch하여 `RuntimeException`으로 래핑하여 re-throw한다.
- `findAll()` 계열은 `List<T>` 반환, 없으면 빈 리스트 반환 (null 반환 금지).
- `findById()` 계열은 `Optional<T>` 반환.

### 주요 쿼리 사항
- `H2OrderRepository.countByDatePrefix(String datePrefix)`: `SELECT COUNT(*) FROM orders WHERE id LIKE ?` → OrderIdGenerator에서 시퀀스 계산에 사용
- `H2ProductionScheduleRepository.findAllOrderByCreatedAt()`: `ORDER BY created_at ASC` → FIFO 생산 처리

## 테스트 전략
Repository 구현체는 인터페이스 기반으로 테스트한다.
H2 인메모리 DB(`jdbc:h2:mem:testdb`)를 사용하여 실제 JDBC 동작을 검증한다.

### 테스트 대상
- `H2SampleRepositoryTest`: save/findById/findAll/update/delete CRUD
- `H2OrderRepositoryTest`: findByStatus, updateStatus, countByDatePrefix
- `H2ProductionScheduleRepositoryTest`: findAllOrderByCreatedAt FIFO 순서 검증

## 완료 조건
- `./gradlew test` 통과
- 모든 Repository 구현체 테스트 GREEN
- `createStatement()` 사용 없음 (subagent4 컴플라이언스 통과)
