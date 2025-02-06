package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.api.domain.usecase.ReservationFacade;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.models.User;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservationIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("root")
            .withPassword("password1!");

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:6.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private Long seatId;

    @BeforeEach
    void setUp() {
        Seat seat = new Seat();
        seat.setConcertScheduleId(1L); // 테스트용 스케줄 ID (실제 스케줄과 연결 필요)
        seat.setSeatNumber(1);
        seat.setPrice(75000L);
        seat.setIsReserved(false);
        seat.setReservedBy(null);

        Seat savedSeat = seatRepository.save(seat);
        seatId = savedSeat.getId();

        redisTemplate.delete("seat:reserved:" + seatId); // Redis 초기화
    }

    @DisplayName("같은 좌석에 대해 동시에 10명이 예약 요청하는 경우, 1명만 성공하고 나머지는 실패한다.")
    @Test
    void when10UsersReserveSeat_thenOnlyOneSucceeds() throws InterruptedException {
        int totalUsers = 10;
        for (int i = 0; i < totalUsers; i++) {
            User user = new User();
            user.setName("user" + (i + 1));
            userRepository.save(user);
        }

        ExecutorService executor = Executors.newFixedThreadPool(totalUsers);
        CountDownLatch latch = new CountDownLatch(totalUsers);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        ValueOperations<String, String> redisOps = redisTemplate.opsForValue();

        for (int i = 0; i < totalUsers; i++) {
            String userIdStr = "user" + (i + 1);

            executor.submit(() -> {
                try {
                    boolean isReserved = redisOps.setIfAbsent("seat:reserved:" + seatId, userIdStr);

                    if (isReserved) {
                        reservationFacade.reserveSeat("valid-token", Long.valueOf(userIdStr.replace("user", "")), seatId);
                        successCount.incrementAndGet();

                        // ✅ 좌석이 예약되었으면 Redis 캐시 삭제
                        redisTemplate.delete("seat:reserved:" + seatId);
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 한 명만 성공해야 함
        assertThat(successCount.get()).isOne();
        assertThat(failureCount.get()).isEqualTo(totalUsers - 1);

        // 실제 DB에 저장된 사용자 ID 검증 추가
        Optional<Reservation> reservations = reservationRepository.findBySeatId(seatId);
        assertThat(reservations).isPresent();
        assertThat(reservations.get().getUserId()).isNotNull();
    }
}