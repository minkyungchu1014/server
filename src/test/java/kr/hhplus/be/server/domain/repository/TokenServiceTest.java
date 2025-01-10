package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.repository.TokenRepository;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    public TokenServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExpireTokens() {
        tokenService.expireTokens();
        verify(tokenRepository).deleteByExpiresAtBefore(any());
    }

    @Test
    public void testCreateToken() {
        Long userId = 1L;
        when(tokenRepository.existsByUserId(userId)).thenReturn(false);
        String token = tokenService.createToken(userId);
        assertNotNull(token);
        verify(tokenRepository, times(1)).saveToken(eq(userId), anyString(), any());
    }

    @Test
    public void testInvalidateExistingTokens() {
        Long userId = 1L;
        tokenService.invalidateExistingTokens(userId);
        verify(tokenRepository, times(1)).deleteByUserId(userId);
    }
}