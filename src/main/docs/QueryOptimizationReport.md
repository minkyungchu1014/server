# **쿼리 성능 개선 보고서**

## **1. 개요**
본 보고서는 데이터베이스 성능 최적화를 위해 자주 사용되는 쿼리를 분석하고, 적절한 인덱스를 추가하여 성능을 개선하는 과정을 정리한 문서입니다. 주요 목표는 **불필요한 테이블 스캔을 줄이고, 인덱스 검색을 활용하여 쿼리 실행 시간을 단축**하는 것입니다.

---

## **2. 자주 조회하는 쿼리 및 복잡한 쿼리 분석**

### **2.1 분석 대상 테이블 및 데이터 건수**
```md
| 테이블명 | 총 레코드 수 |
|----------|-------------|
| payment | 99,575 |
| reservation | 119,520 |
| seat | 99,938 |
| concert_schedule | 16,500 |
```

### **2.2 주요 쿼리 및 실행 계획 분석**
#### ✅ **ConcertSchedule 조회 쿼리**
```sql
SELECT cs.id FROM concert_schedule cs WHERE cs.schedule_date = '2024-01-01';
```
**인덱스 추가 전 실행 계획:**
- **Full Table Scan 발생** → `Table scan on cs  (cost=10102 rows=99974) (actual time=2.88..60.8 rows=100000 loops=1)`
- 예상 비용 **10,102**

**인덱스 추가 후 실행 계획:**
- **Covering index lookup 적용** → `Covering index lookup on cs using idx_concert_schedule_date (schedule_date=DATE'2024-01-01')  (cost=1.1 rows=1)`
- 예상 비용 **1.1** (99.9% 감소)

---
#### ✅ **Payment 테이블 - 결제 실패 여부 확인**
```sql
SELECT COUNT(p) > 0 FROM payment p WHERE p.reservation_id = 1234 AND p.status = 'FAILED';
```
**인덱스 추가 전 실행 계획:**
- **Full Table Scan 발생** → `Filter: ((payment.status = 'FAILED') and (payment.reservation_id = 1234))  (cost=10094 rows=996)`
- 예상 비용 **10,094**

**인덱스 추가 후 실행 계획:**
- **Covering index lookup 적용** → `Covering index lookup on payment using idx_payment_reservation_status (reservation_id=1234, status='FAILED') (cost=1.1 rows=1)`
- 예상 비용 **1.1** (99.9% 감소)

---
#### ✅ **Reservation - 예약 가능한 날짜 조회**
```sql
SELECT DISTINCT DATE(r.expires_at) FROM reservation r WHERE r.status = 'AVAILABLE' AND r.expires_at > CURRENT_DATE;
```
**인덱스 추가 전 실행 계획:**
- **Temporary table 사용으로 성능 저하** → `Temporary table with deduplication  (cost=11722..11722 rows=3984)`
- 예상 비용 **11,722**

**인덱스 추가 후 실행 계획:**
- **인덱스를 활용한 조회 최적화** → `Temporary table with deduplication (cost=1.31..1.31 rows=1)`
- 예상 비용 **1.31** (99.9% 감소)

---
#### ✅ **Seat - 예약 가능한 좌석 조회**
```sql
SELECT s.* FROM seat s WHERE s.concert_schedule_id IN (1,2,3) AND s.is_reserved = false;
```
**인덱스 추가 전 실행 계획:**
- **Full Table Scan 발생** → `Table scan on s  (cost=10114 rows=99938) (actual time=1.39..77.8 rows=100000 loops=1)`
- 예상 비용 **10,114**

**인덱스 추가 후 실행 계획:**
- **Index range scan 적용** → `Index range scan on s using idx_seat_schedule_reserved` (조건 `concert_schedule_id` 및 `is_reserved` 활용)
- 예상 비용 **대폭 감소**

---

## **3. 인덱스 추가 및 최적화 전략**
### **3.1 추가한 인덱스**
```sql
CREATE INDEX idx_concert_schedule_date ON concert_schedule(schedule_date);
CREATE INDEX idx_payment_reservation_status ON payment(reservation_id, status);
CREATE INDEX idx_reservation_status_expires ON reservation(status, expires_at);
CREATE INDEX idx_seat_schedule_reserved ON seat(concert_schedule_id, is_reserved);
```

---

## **4. 성능 개선 결과 비교**
### **4.1 실행 시간 비교**
```md
| 쿼리 유형 | 기존 실행 시간(ms) | 인덱스 적용 후 실행 시간(ms) | 개선율(%) |
|-----------|------------------|----------------------|----------|
| ConcertSchedule 조회 | 75.1 | 0.0244 | 99.9% |
| Payment 상태 조회 | 92.9 | 0.0228 | 99.9% |
| Reservation 날짜 조회 | 87.6 | 0.0924 | 99.8% |
| Seat 예약 가능 여부 | 91.6 | 0.015 | 99.9% |
```

### **4.2 I/O 감소 확인 (`SHOW STATUS LIKE 'Handler_read%'`)**
```md
| 항목 | 기존 값 | 인덱스 적용 후 |
|------|--------|--------------|
| Handler_read_next | 1530 | 감소 |
| Handler_read_rnd | 64 | 감소 |
| Handler_read_rnd_next | 677019 | **대폭 감소** |
```

---

## **5. 결론 및 향후 계획**
### **5.1 최종 평가**
✅ **불필요한 Full Table Scan을 제거하고, 인덱스를 활용하여 실행 비용을 평균 99.9% 감소**
✅ **실행 시간을 대폭 단축하여 서비스 응답 속도 개선**
✅ **Handler_read_rnd 및 Handler_read_rnd_next 값이 대폭 감소하여 I/O 성능 최적화 확인**

### **5.2 향후 개선 방향**
- **추가적인 부하 테스트 수행** (동시 요청 처리 성능 확인)
- **인덱스 유지 비용 분석 후 불필요한 인덱스 제거 여부 검토**
- **Redis 캐싱을 활용한 추가 성능 개선 방안 검토**

**보고서 작성자: [작성자 이름]**  
**작성일: [YYYY-MM-DD]**

