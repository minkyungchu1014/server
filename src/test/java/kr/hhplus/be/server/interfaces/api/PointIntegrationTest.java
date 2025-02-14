package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Point;
import kr.hhplus.be.server.domain.models.PointHistory;
import kr.hhplus.be.server.domain.models.User;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import kr.hhplus.be.server.domain.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
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
public class PointIntegrationTest {

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
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    private final Long userId = 1L;

    private static final String TEST_TOKEN = "Bearer TEST_TOKEN";

    private HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        pointRepository.deleteAll();

        User user = new User("Test User");
        user = userRepository.save(user);
        userRepository.flush();

        Point point = new Point();
        point.setTotal(100L);
        point.setUser(user);
        pointRepository.save(point);
        pointRepository.flush();

        headers = new HttpHeaders();
        headers.set("Authorization", TEST_TOKEN);
    }


    @Test
    void testGetPoint_Success() {
        headers.set("Authorization", TEST_TOKEN);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

//        ResponseEntity<Long> response = restTemplate.exchange(
//                "/api/point/" + userId, HttpMethod.GET, requestEntity, Long.class
//        );
        ResponseEntity<String> response = restTemplate.getForEntity("/api/point/" + userId, String.class);
        System.out.println("Response Body: " + response.getBody());
        System.out.println("Response Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(100L);
    }

    @Test
    void testAddPoint_Success() {
        Long amountToAdd = 50L;
        restTemplate.postForEntity("/api/point/" + userId + "/add", amountToAdd, Void.class);
        Optional<Point> pointOptional = pointRepository.findPointWithLock(userId);
        assertThat(pointOptional).isPresent();
        assertThat(pointOptional.get().getTotal()).isEqualTo(150L);
    }

    @Test
    void testDeductPoint_Success() {
        Long amountToDeduct = 50L;

        pointService.deductPoint(userId, amountToDeduct, "테스트 차감");

        Optional<Point> pointOptional = pointRepository.findPointWithLock(userId);
        assertThat(pointOptional).isPresent();
        assertThat(pointOptional.get().getTotal()).isEqualTo(50L);

        // ✅ 포인트 차감 후 히스토리 저장 여부 확인
        List<PointHistory> history = pointHistoryRepository.findByUserId(userId);
        assertThat(history).isNotEmpty();
        assertThat(history.get(0).getAmount()).isEqualTo(-50L);
    }


    @Test
    void testDeductPoint_Failure_InsufficientPoints() {
        Long amountToDeduct = 200L;
        ResponseEntity<String> response = restTemplate.postForEntity("/api/point/" + userId + "/deduct", amountToDeduct, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("포인트가 부족합니다.");
    }

    @Test
    void testConcurrentAddPointRequests() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Long amountToAdd = 50L;
        String url = "/api/point/" + userId + "/add";

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ResponseEntity<Void> response = restTemplate.postForEntity(url, amountToAdd, Void.class);
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
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
        executorService.shutdown();

        Optional<Point> pointOptional = pointRepository.findPointWithLock(userId);
        assertThat(pointOptional).isPresent();
        assertThat(pointOptional.get().getTotal()).isEqualTo(150L);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
    }
}