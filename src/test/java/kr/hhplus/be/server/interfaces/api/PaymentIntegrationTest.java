package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.api.domain.dto.PaymentRequest;
import kr.hhplus.be.server.api.domain.usecase.PaymentFacade;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.User;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import kr.hhplus.be.server.domain.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
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
public class PaymentIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("root")
            .withPassword("password1!")
            .withReuse(true);

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:6.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @LocalServerPort
    private int port; // ✅ 포트 자동 설정

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private PaymentFacade paymentFacade; // ✅ Mock 처리

    @Autowired
    private PointService pointService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    private Long userId;
    private Long reservationId;
    private HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        paymentRepository.deleteAll();

        // 사용자 및 예약 데이터 동적 생성
        User user = new User();
        user.setName("testUser");
        user = userRepository.save(user);
        userId = user.getId();

        // 예약 객체 생성 시 필수 필드 설정 추가
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seatId(1L)  // 임시 값 설정
                .concertScheduleId(1L)  // 임시 값 설정
                .status("PENDING")  // 기본 예약 상태 설정
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // 기본 만료 시간 설정
                .build();
        reservation = reservationRepository.save(reservation);
        reservationId = reservation.getId();

        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer TEST_TOKEN");
    }


    @Test
    void testConcurrentPaymentRequests() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final Long userId = userRepository.save(new User("testUser-" + i)).getId();
            final Long reservationId = reservationRepository.save(new Reservation(userId)).getId();

            PaymentRequest paymentRequest = new PaymentRequest(userId, reservationId);
            HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, new HttpHeaders());
            String url = "http://localhost:" + port + "/api/payment/process";

            executorService.submit(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        assertThat(successCount.get()).isEqualTo(threadCount);
    }


    @Test
    void testProcessPayment_Success() {
        // Arrange
        PaymentRequest paymentRequest = new PaymentRequest(userId, reservationId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);

        String url = "http://localhost:" + port + "/api/payment/process";

        // Mock 설정
        when(paymentFacade.processPayment(userId, reservationId)).thenAnswer(invocation -> {
            pointService.deductPoint(userId, 75000L, "결제"); // ✅ 포인트 차감 후 결제 성공
            return "결제 성공";
        });

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("결제 성공");

        // ✅ 포인트 잔액 검증 추가
        Long updatedBalance = pointService.getPoint(userId);
        assertThat(updatedBalance).isEqualTo(25000L); // 10만 포인트 - 75000L 차감
    }


    @Test
    void testProcessPayment_Failure() {
        // Arrange
        PaymentRequest paymentRequest = new PaymentRequest(userId, reservationId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);

        String url = "http://localhost:" + port + "/api/payment/process";

        // Mock 설정
        when(paymentFacade.processPayment(userId, reservationId)).thenThrow(new RuntimeException("결제 실패"));

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("결제 실패");
    }

}