package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateToken_Success() {
        Long userId = 1L;
        when(tokenRepository.existsByToken(anyString())).thenReturn(false);

        String token = tokenService.generateToken(userId);

        assertThat(token).isNotNull();
        verify(tokenRepository).save(any(Token.class));
        verify(redisRepository).setValue(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testDeleteByExpiresAtBefore() {
        tokenService.deleteByExpiresAtBefore();

        verify(tokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    void testTokenExists() {
        String token = "mockToken";
        when(tokenRepository.existsByToken(token)).thenReturn(true);

        boolean exists = tokenService.tokenExists(token);

        assertThat(exists).isTrue();
        verify(tokenRepository).existsByToken(token);
    }

    @Test
    void testIsTokenExpired() {
        String token = "mockToken";
        when(tokenRepository.isTokenExpired(token)).thenReturn(true);

        boolean isExpired = tokenService.isTokenExpired(token);

        assertThat(isExpired).isTrue();
        verify(tokenRepository).isTokenExpired(token);
    }
}
