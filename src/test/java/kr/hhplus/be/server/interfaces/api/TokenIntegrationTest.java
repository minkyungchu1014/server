package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.User;
import kr.hhplus.be.server.domain.repository.RedisRepository;
import kr.hhplus.be.server.domain.repository.TokenRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

    @Mock
    private RedisRepository redisRepository;

    private Long userId;
    private Clock fixedClock;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User();
        user.setName("testUser");
        user = userRepository.save(user);
        userId = user.getId();

        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer TEST_TOKEN");
    }

    @Test
    void testGenerateToken_Success() {
        // Arrange
        Long userId = userRepository.save(new User("testUser")).getId();
        when(redisRepository.getValue(anyString())).thenReturn("WAIT");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/api/token/generate?userId=" + userId, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = response.getBody();
        assertThat(token).isNotNull();
        assertThat(tokenRepository.existsByToken(token)).isTrue();
        verify(redisRepository).setValue(eq(token), eq("WAIT"), anyLong(), any());
    }

    @Test
    void testConcurrentTokenGeneration() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long userId = userRepository.save(new User("testUser-" + i)).getId();
            when(redisRepository.getValue(anyString())).thenReturn("WAIT");

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
        tokenRepository.findAll().forEach(token -> verify(redisRepository).setValue(eq(token.getToken()), eq("WAIT"), anyLong(), any()));
    }
}
