package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGenerateToken_Success() {
        // Arrange
        Long userId = 1L;
        when(tokenRepository.existsByToken(anyString())).thenReturn(false);

        // Act
        String token = tokenService.generateToken(userId);

        // Assert
        assertThat(token).isNotNull();
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void testDeleteByExpiresAtBefore() {
        // Act
        tokenService.deleteByExpiresAtBefore();

        // Assert
        verify(tokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    void testTokenExists() {
        // Arrange
        String token = "mockToken";
        when(tokenRepository.existsByToken(token)).thenReturn(true);

        // Act
        boolean exists = tokenService.tokenExists(token);

        // Assert
        assertThat(exists).isTrue();
        verify(tokenRepository).existsByToken(token);
    }

    @Test
    void testIsTokenExpired() {
        // Arrange
        String token = "mockToken";
        when(tokenRepository.isTokenExpired(token)).thenReturn(true);

        // Act
        boolean isExpired = tokenService.isTokenExpired(token);

        // Assert
        assertThat(isExpired).isTrue();
        verify(tokenRepository).isTokenExpired(token);
    }
}