package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.repository.TokenRepository;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class TokenServiceIntegrationTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private TokenRepository tokenRepository;

    @Test
    public void testCreateToken() {
        Long userId = 1L;
        String token = tokenService.createToken(userId);
        assertNotNull(token);
        assertTrue(tokenService.tokenExists(token));
    }

    @Test
    public void testInvalidateExistingTokens() {
        Token token = new Token();
        token.setUserId(1L);
        token.setToken("test-token");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        tokenRepository.save(token);

        tokenService.invalidateExistingTokens(1L);

        assertFalse(tokenRepository.existsByUserId(1L));
    }

    @Test
    public void testExpireTokens() {
        Token token = new Token();
        token.setUserId(1L);
        token.setToken("test-token");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(token);

        tokenService.expireTokens();

        assertFalse(tokenRepository.existsByToken("test-token"));
    }
}