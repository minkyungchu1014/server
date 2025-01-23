package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository; // Mock으로 변경

    @InjectMocks
    private TokenService tokenService; // Mock 객체 주입

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-22T00:00:00Z"), ZoneId.of("UTC"));
        LocalDateTime fixedNow = LocalDateTime.now(fixedClock);
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
