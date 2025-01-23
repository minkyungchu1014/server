package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.repository.TokenRepository;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
            .withPassword("password1!");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TokenService tokenService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        fixedClock = Clock.fixed(Instant.parse("2025-01-22T00:00:00Z"), ZoneId.of("UTC"));
    }

    @Test
    void testGenerateToken_Success() {
        // Arrange
        Long userId = 1L;

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/api/token/generate?userId=" + userId, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = response.getBody();
        assertThat(token).isNotNull();
        assertThat(tokenRepository.existsByToken(token)).isTrue();
    }


    @Test
    void testDeleteExpiredTokens_Success() {
        // Arrange
        Token token = new Token();
        token.setUserId(1L);
        token.setToken("expired-token");
        token.setExpiresAt(LocalDateTime.now(fixedClock).minusMinutes(1));
        tokenRepository.save(token);

        // Act
        restTemplate.delete("/api/token/delete-expired");

        // Assert
        assertThat(tokenRepository.existsByToken("expired-token")).isFalse();
    }

    @Test
    void testTokenExists_Success() {
        // Arrange
        String token = tokenService.generateToken(1L);

        // Act
        ResponseEntity<Boolean> response = restTemplate.getForEntity("/api/token/exists?token=" + token, Boolean.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    void testIsTokenExpired_Success() {
        // Arrange
        Token token = new Token();
        token.setUserId(1L);
        token.setToken("expired-token");
        token.setExpiresAt(LocalDateTime.now(fixedClock).minusMinutes(1));
        tokenRepository.save(token);

        // Act
        ResponseEntity<Boolean> response = restTemplate.getForEntity("/api/token/is-expired?token=" + token.getToken(), Boolean.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Configuration
    static class TestConfig {
        @Bean
        public TestRestTemplate restTemplate() {
            TestRestTemplate restTemplate = new TestRestTemplate();
            restTemplate.getRestTemplate().getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            return restTemplate;
        }
    }
}
