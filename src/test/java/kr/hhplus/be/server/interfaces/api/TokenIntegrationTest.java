package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.User;
import kr.hhplus.be.server.domain.repository.TokenRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TokenIntegrationTest {

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

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private Long userId;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("testUser");
        user = userRepository.save(user);
        userId = user.getId();

        // 고정된 Clock 객체 생성 (현재 시간을 특정 시점으로 고정)
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    }

    @Test
    void testGenerateToken_Success() {
        Long userId = userRepository.save(new User("testUser")).getId();
        ResponseEntity<String> response = restTemplate.postForEntity("/api/token/generate?userId=" + userId, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = response.getBody();
        assertThat(token).isNotNull();
        assertThat(tokenRepository.existsByToken(token)).isTrue();
        assertThat(redisTemplate.opsForValue().get(token)).isEqualTo("WAIT");
    }

    @Test
    void testConcurrentTokenGeneration() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long userId = userRepository.save(new User("testUser-" + i)).getId();
            executorService.submit(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity("/api/token/generate?userId=" + userId, null, String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertThat(tokenRepository.count()).isEqualTo(threadCount);
        tokenRepository.findAll().forEach(token -> assertThat(redisTemplate.opsForValue().get(token.getToken())).isEqualTo("WAIT"));
    }
}
