package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.repository.QueueRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.service.ExpirationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ExpirationIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("root")
            .withPassword("password1!");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private ExpirationService expirationService;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    public void setUp() {
        queueRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    void testProcessExpiredReservationsAndTokens_Success() throws InterruptedException {
        // Arrange: 만료된 대기열 및 예약 데이터 준비
        Queue queue = new Queue();
        queue.setUserId(1L);
        queue.setTokenId("test-token");
        queue.setStatus("ACTIVE");
        queue.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // 1분 전에 만료된 상태
        queueRepository.save(queue);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUserId(1L);
        reservation.setStatus("CONFIRMED");
        reservation.setSeatId(100L);
        reservationRepository.save(reservation);

        // 동시성 테스트를 위한 스레드 풀 및 CountDownLatch 설정
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Act: 여러 스레드에서 만료 처리 실행
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    expirationService.processExpiredReservationsAndTokens();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 완료할 때까지 대기

        // Assert: 대기열 데이터가 삭제되었는지 확인
        Optional<Queue> expiredQueue = queueRepository.findById(queue.getId());
        assertThat(expiredQueue).isEmpty();

        // Assert: 예약 상태가 EXPIRED로 변경되었는지 확인
        Optional<Reservation> expiredReservation = reservationRepository.findById(reservation.getId());
        assertThat(expiredReservation).isPresent();
        assertThat(expiredReservation.get().getStatus()).isEqualTo("EXPIRED");
    }
}