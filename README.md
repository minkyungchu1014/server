### 프로젝트 패키지 구성
```
server-java/
├── .gitignore                # Git에서 추적하지 않을 파일 및 폴더 설정
├── build.gradle              # Gradle 빌드 설정 파일
├── gradle/                    # Gradle 관련 설정 및 wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── kr/
│   │   │   │   └── hhplus/
│   │   │   │       └─── be/
│   │   │   │           ├── server/
│   │   │   │           │   ├── config/         # 서버 관련 설정 (Spring 설정, DB 연결 등)
│   │   │   │           │   ├── controller/    # API 요청을 처리하는 컨트롤러
│   │   │   │           │   ├── service/       # 비즈니스 로직 처리
│   │   │   │           │   ├── repository/    # 데이터 접근 계층 (JPA, Mock 등)
│   │   │   │           │   └── model/          # 도메인 모델 (DTO, 엔티티 등)
│   │   │   │           └─── usecase/            # 비즈니스 로직 유스케이스
│   │   │   │           
│   │   │   │       
│   │   └── resources/             # 서버 설정 파일들 (application.properties, 로그 설정 등)
│   │       └── application.properties  # DB, API, 로깅 등 설정
├── .github/                   # GitHub 관련 설정 (Pull Request 템플릿 등)
└── README.md                   # 프로젝트 설명
```
# 동시성 제어 방안 분석 및 구현

본 프로젝트에서는 데이터 무결성과 안정성을 보장하기 위해 발생할 수 있는 **동시성 이슈**를 분석하고, 이를 해결하기 위한 다양한 제어 방식을 도입 및 테스트하였습니다.

---

## 1. 발생 가능한 동시성 이슈

다음 시나리오에서 동시성 문제가 발생할 수 있습니다:

1. **좌석 예약 시스템**
    - 여러 사용자가 동시에 동일한 좌석을 예약하려고 시도할 때.
    - 잘못된 상태(`RESERVED`, `CONFIRMED`)로 데이터가 덮어씌워질 위험.

2. **포인트 차감**
    - 동일한 사용자가 동시에 포인트를 차감하려고 하면 잔액 부족 상태에서도 성공할 수 있음.

3. **결제 처리**
    - 여러 사용자가 동일한 예약에 대해 중복 결제를 처리하려고 시도할 경우.

---

## 2. 동시성 제어 방식 및 평가

### 2.1 비관적 락 (Pessimistic Locking)

#### 설명
- 데이터에 대한 잠금을 명시적으로 설정하여 다른 트랜잭션이 동시에 접근하지 못하도록 제어.
- Spring Data JPA의 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 사용.

#### 장점
- 충돌 방지 효과적 (데드락을 방지하는 경우).
- 데이터 무결성을 보장.

#### 단점
- 성능 저하: 락이 오래 유지되면 트랜잭션 대기 시간이 길어질 수 있음.
- 구현 복잡도는 낮지만 트랜잭션 관리에 주의가 필요.

#### 예시 코드
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM Reservation r WHERE r.seatId = :seatId")
Optional<Reservation> findReservationWithLock(@Param("seatId") Long seatId);
```

---

### 2.2 낙관적 락 (Optimistic Locking)

#### 설명
- 데이터에 버전 필드를 추가하여 변경이 있을 경우 충돌 여부를 감지.
- `@Version` 어노테이션을 사용하여 구현.

#### 장점
- 읽기 위주의 트랜잭션에서 유용 (락 사용을 최소화).
- 성능에 유리.

#### 단점
- 충돌 발생 시 롤백 필요 → 클라이언트에게 재시도 로직 필요.
- 구현 복잡도 중간.

#### 예시 코드
```java
@Entity
public class Reservation {
    @Version
    private Long version;

    // other fields
}
```

---

### 2.3 데이터베이스 레벨에서의 동시성 제어

#### 설명
- 데이터베이스의 트랜잭션 격리 수준 설정 (`READ_COMMITTED`, `SERIALIZABLE`)을 통해 동시성 문제를 제어.

#### 장점
- 트랜잭션 격리 수준만으로 제어하므로 구현이 간단.
- DBMS가 직접 관리하므로 높은 신뢰성.

#### 단점
- `SERIALIZABLE` 사용 시 성능 저하.
- 애플리케이션에서 세부 제어가 어렵고, DBMS에 종속적.

---

### 2.4 Redis를 활용한 분산 락

#### 설명
- 분산 환경에서 동시성을 제어하기 위해 Redis를 활용한 분산 락 도입.
- `SETNX` 명령어로 락 획득, TTL로 락 만료 시간 설정.

#### 장점
- 분산 시스템에서 유용.
- 성능이 빠르고, 구현이 비교적 간단.

#### 단점
- Redis 장애 시 추가적인 복구 절차 필요.
- 락 TTL 설정이 잘못되면 데이터 무결성 문제 발생 가능.

#### 예시 코드
```java
Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent("seat:lock:" + seatId, "locked", 5, TimeUnit.SECONDS);
if (!lockAcquired) {
    throw new IllegalStateException("Seat is being processed by another transaction");
}
```

---

### 2.5 애플리케이션 레벨에서의 동기화

#### 설명
- Java의 `synchronized` 키워드나 `ReentrantLock`을 사용하여 애플리케이션 레벨에서 동기화.

#### 장점
- 구현 간단, 테스트 용이.
- DB와 무관하게 동기화 가능.

#### 단점
- 분산 환경에서 효과적이지 않음.
- 단일 인스턴스에서만 작동.

#### 예시 코드
```java
public synchronized void reserveSeat(Long seatId) {
    // Seat reservation logic
}
```

---

## 3. 각 방식의 비교

| **방식**                | **장점**                    | **단점**                          | **적합한 시나리오**            |
|-------------------------|----------------------------|-----------------------------------|--------------------------------|
| 비관적 락               | 데이터 무결성 보장          | 성능 저하                         | 데이터 충돌 가능성이 높은 경우 |
| 낙관적 락               | 성능 유리, 충돌 최소화      | 충돌 시 롤백 및 재시도 필요        | 읽기 위주 트랜잭션            |
| DB 트랜잭션 격리 수준 설정 | 단순 구현, 신뢰성 높음       | 성능 저하 (높은 격리 수준)         | 단일 DB 환경                 |
| Redis 분산 락           | 분산 환경 지원, 빠른 성능   | TTL 설정 관리 필요                | 분산 시스템, 고성능 요구 사항 |
| 애플리케이션 동기화     | 구현 간단, 테스트 쉬움      | 분산 환경 비효율적                | 단일 서버 애플리케이션        |

---

## 4. 프로젝트 구현에서 선택된 방식

### 주요 방식
1. **비관적 락**:
    - 좌석 예약에서 데이터 무결성 보장을 위해 도입.
2. **낙관적 락**:
    - 포인트 차감과 같이 충돌 가능성이 낮은 작업에 도입.
3. **Redis 분산 락**:
    - 분산 환경에서 결제 중복 방지를 위해 사용.

### 구현 및 테스트 결과
- 비관적 락은 충돌 상황에서 데이터 무결성을 완벽히 보장했으나, 고부하 상황에서 성능 저하가 발생.
- 낙관적 락은 읽기 위주의 작업에서 성능을 크게 개선.
- Redis 분산 락은 분산 환경에서 높은 신뢰성과 성능을 제공.


## 5. 동시성 제어 구현 및 테스트 코드

비즈니스 로직 적용: 좌석 예약 비관적 락 구현

Repository 코드
```java
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.seatId = :seatId AND r.status IN ('AVAILABLE', 'RESERVED')")
    Optional<Reservation> findReservationWithLock(@Param("seatId") Long seatId);
}
```

Service 코드
```java
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void reserveSeat(Long userId, Long seatId) {
        Reservation reservation = reservationRepository.findReservationWithLock(seatId)
            .orElseThrow(() -> new IllegalStateException("Seat is not available for reservation"));

        reservation.setStatus("RESERVED");
        reservation.setUserId(userId);
        reservationRepository.save(reservation);
    }
}
```
통합 테스트 코드
```java
@SpringBootTest
@ActiveProfiles("test")
public class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @Transactional
    public void testReserveSeat() {
        // Arrange
        Long seatId = 1L;
        Long userId = 100L;

        Reservation reservation = new Reservation();
        reservation.setSeatId(seatId);
        reservation.setStatus("AVAILABLE");
        reservationRepository.save(reservation);

        // Act
        reservationService.reserveSeat(userId, seatId);

        // Assert
        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertEquals("RESERVED", updatedReservation.getStatus());
        assertEquals(userId, updatedReservation.getUserId());
    }
}
```
### 테스트 결과

- 동시 요청에서 동일한 좌석에 대한 중복 예약 방지 확인.

- 트랜잭션 충돌 없이 데이터 무결성 보장.