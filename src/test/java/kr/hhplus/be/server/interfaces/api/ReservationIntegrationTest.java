package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.api.domain.usecase.ReservationFacade;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.models.User;
import kr.hhplus.be.server.domain.repository.RedisRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
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
import static org.mockito.Mockito.when;

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

    @Mock
    private RedisRepository redisRepository;

    private Long seatId;

    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Seat seat = new Seat();
        seat.setConcertScheduleId(1L);
        seat.setSeatNumber(1);
        seat.setPrice(75000L);
        seat.setIsReserved(false);
        seat.setReservedBy(null);

        Seat savedSeat = seatRepository.save(seat);
        seatId = savedSeat.getId();

        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer TEST_TOKEN");
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

        for (int i = 0; i < totalUsers; i++) {
            String userIdStr = "user" + (i + 1);

            executor.submit(() -> {
                try {
                    boolean isReserved = true;
                    when(redisRepository.getValue("seat:reserved:" + seatId)).thenReturn(null);
                    redisRepository.setValue("seat:reserved:" + seatId, userIdStr, 5, java.util.concurrent.TimeUnit.MINUTES);

                    if (isReserved) {
                        reservationFacade.reserveSeat("valid-token", Long.valueOf(userIdStr.replace("user", "")), seatId);
                        successCount.incrementAndGet();

                        redisRepository.removeFromQueue("seat:reserved:" + seatId, userIdStr);
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

        assertThat(successCount.get()).isOne();
        assertThat(failureCount.get()).isEqualTo(totalUsers - 1);

        Optional<Reservation> reservations = reservationRepository.findBySeatId(seatId);
        assertThat(reservations).isPresent();
        assertThat(reservations.get().getUserId()).isNotNull();
    }
}
