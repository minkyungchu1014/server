# MSA 환경에서의 트랜잭션 처리 한계 및 해결 방안

## 1. 개요
서비스의 규모가 확장됨에 따라 **결제(Payment), 예약(Reservation), 대기열(Queue), 포인트(Point), 공연(Concert)** 등의 도메인별로 MSA(Microservices Architecture) 형태로 분리해야 합니다. 그러나 MSA에서는 **분산 트랜잭션** 문제가 발생할 수 있습니다. 이 문서는 이러한 한계와 해결 방안을 정의합니다.

## 2. 주요 기능 및 트랜잭션 범위 분석

| 서비스 | 주요 기능 | 트랜잭션 범위 |
|--------|----------|--------------|
| **Reservation Service** | 예약 생성, 예약 취소 | 예약 상태와 대기열 및 결제와의 일관성 유지 필요 |
| **Queue Service** | 대기열 관리 | 대기열에 사용자를 추가하고, 활성화 시켜 트랜잭션 흐름을 관리 |
| **Payment Service** | 결제 처리, 포인트 차감 | 결제가 성공하면 예약 확정 및 포인트 차감 보장 필요 |
| **Point Service** | 포인트 적립/차감 | 결제 성공 시 포인트 차감 또는 적립 |
| **Concert Service** | 공연 정보 조회 | 예약과 결제가 유효한 공연인지 확인 |

---

## 3. 트랜잭션 처리의 한계

### (1) 분산 트랜잭션으로 인한 데이터 일관성 문제
- 서비스가 개별 DB를 가지므로 **일관된 트랜잭션 관리가 어려움**
- **예제 시나리오:**
    - 사용자가 예약 요청을 보내고 결제를 완료했지만, **포인트 차감이 실패하면 결제를 롤백해야 함**
    - 사용자가 대기열에서 활성화되지 않았는데 결제 요청이 들어온다면 트랜잭션을 어떻게 처리할지 고민 필요
    - 하나의 서비스에서 장애가 발생하면 다른 서비스의 트랜잭션 상태와 불일치 가능

### (2) 대기열(Queue)과 예약 시스템 간의 연동 문제
- 사용자가 대기열에 존재하지만 **예약이 이루어지지 않은 상태에서 결제 진행 시 문제 발생**
- 대기열에서 사용자가 **ACTIVE 상태로 변경되지 않으면 예약 및 결제가 불가능하도록 해야 함**

### (3) 이벤트 일관성 문제
- MSA에서는 Kafka 또는 RabbitMQ 같은 메시지 브로커를 이용한 비동기 이벤트 방식을 사용
- 그러나 **이벤트가 중복 처리되거나 손실될 가능성**이 있음
- **예제:** Queue에서 사용자가 활성화된 후 예약 이벤트가 Reservation Service에 정상적으로 반영되지 않으면 대기열과 예약 정보가 불일치할 수 있음

---

## 4. 해결 방안

### (1) SAGA 패턴 적용
SAGA 패턴은 분산 트랜잭션을 보장하기 위해 각 서비스의 상태를 조정하는 방식입니다.  
보상 트랜잭션(Compensating Transaction) 또는 오케스트레이션 방식을 적용하여 트랜잭션을 관리할 수 있습니다.

#### ✅ 오케스트레이션 기반 SAGA (Orchestration-based SAGA)
- **SAGA Coordinator**가 트랜잭션 단계를 관리
- **예제:**
    1. Queue Service에서 **사용자 대기열 등록** (`WAITING` 상태)
    2. 사용자가 **대기열에서 활성화됨** (`ACTIVE` 상태)
    3. Reservation Service에서 예약 생성 (`PENDING` 상태)
    4. Payment Service에서 결제 요청
    5. Point Service에서 포인트 차감
    6. 결제 성공 시 Reservation 상태를 `CONFIRMED`로 변경
    7. 실패 시 **보상 트랜잭션**을 실행하여 예약 취소 및 포인트 환불

---

### (2) 이벤트 기반 비동기 처리 (Event-Driven Architecture)
- Kafka/RabbitMQ 등을 활용하여 서비스 간 **비동기 이벤트** 방식 적용
- **트랜잭션 상태를 이벤트로 전달**하여 장애 전파를 방지하고, 이벤트 유실 방지를 위해 **이벤트 소싱(Event Sourcing) 패턴 적용**
- **예제:**
    - `QUEUE_ACTIVATED` → `RESERVATION_CREATED` → `PAYMENT_PROCESSED` → `POINT_DEDUCTED` → `RESERVATION_CONFIRMED`

---

### (3) 보상 트랜잭션 (Compensating Transaction)
- SAGA 패턴을 사용해 트랜잭션 실패 시 보상 로직 실행
- **예제:**
    - 결제 성공 후 포인트 차감 실패 시, **결제를 취소하고 환불**
    - 대기열에서 활성화되었지만 예약이 완료되지 않았을 경우, **대기열에서 제거하고 사용자 상태 복구**

---

### (4) Queue Service를 활용한 트랜잭션 흐름 제어
- **Queue Service**가 Reservation 및 Payment 서비스로 직접 요청하는 것이 아니라,  
  이벤트를 활용하여 사용자의 상태를 `ACTIVE`로 변경한 후 예약 및 결제가 진행되도록 함
- **예약 가능 상태(`ACTIVE`)가 아닌 사용자는 예약 요청을 할 수 없도록 제한**

---

## 5. 서비스 간 트랜잭션 흐름

1️⃣ **대기열 등록 요청 (Queue Service)**
- 사용자를 `WAITING` 상태로 대기열에 추가
- Kafka 이벤트 발행 (`QUEUE_WAITING`)

2️⃣ **대기열 활성화 (Queue Service)**
- 특정 사용자 수만큼 `ACTIVE` 상태로 전환
- Kafka 이벤트 발행 (`QUEUE_ACTIVATED`)

3️⃣ **좌석 예약 요청 (Reservation Service)**
- `ACTIVE` 상태의 사용자만 예약 가능
- 예약을 `PENDING` 상태로 변경
- Kafka 이벤트 발행 (`RESERVATION_CREATED`)

4️⃣ **결제 요청 (Payment Service)**
- 예약 정보를 확인 후 결제 진행
- 포인트 차감 요청 (Point Service 호출)
- Kafka 이벤트 발행 (`PAYMENT_PROCESSED`)

5️⃣ **포인트 차감 (Point Service)**
- 사용자의 포인트 차감 처리
- Kafka 이벤트 발행 (`POINT_DEDUCTED`)

6️⃣ **예약 확정 (Reservation Service)**
- 결제 및 포인트 차감 성공 후 예약 `CONFIRMED` 변경
- Kafka 이벤트 발행 (`RESERVATION_CONFIRMED`)

7️⃣ **트랜잭션 실패 처리 (Compensating Transaction)**
- 결제 실패 시 → 대기열 상태 복구 (`WAITING`)
- 포인트 차감 실패 시 → 결제 취소

---

## 6. 결론

MSA 환경에서 트랜잭션을 안정적으로 관리하기 위해  
✅ **SAGA 패턴을 도입하여 보상 트랜잭션 처리**  
✅ **Queue Service를 활용하여 사용자 흐름을 제어**  
✅ **이벤트 기반 아키텍처를 활용해 서비스 간 결합도를 낮춤**  
✅ **Redis 캐싱 및 CQRS로 조회 성능 최적화**

이 방식을 적용하면 **대기열, 예약, 결제 시스템 간의 일관성을 유지하면서 확장성을 보장할 수 있습니다.** 🚀
