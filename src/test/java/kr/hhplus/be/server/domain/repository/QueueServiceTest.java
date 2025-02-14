package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.mockito.Mockito.*;

class QueueServiceTest {

    @InjectMocks
    private QueueService queueService;

    @Mock
    private RedisRepository redisRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddToQueue() {
        String tokenId = "testToken";
        Long userId = 1L;
        String value = userId + ":WAITING:" + tokenId;
        long expiresAt = System.currentTimeMillis() + (30 * 60 * 1000);

        queueService.addToQueue(tokenId, userId);

        verify(redisRepository, times(1)).addToQueue("queue:users", value, expiresAt);
    }

    @Test
    void testActivateTokens() {
        Set<String> mockUsers = Set.of("1:WAITING:testToken");

        when(redisRepository.getQueue("queue:users")).thenReturn(mockUsers);

        queueService.activateTokens(1);

        verify(redisRepository, times(1)).removeFromQueue("queue:users", "1:WAITING:testToken");
        verify(redisRepository, times(1)).addToQueue(anyString(), anyString(), anyDouble());
    }

    @Test
    void testDeleteByExpiresAtBefore() {
        Set<String> expiredUsers = Set.of("1:WAITING:testToken");

        when(redisRepository.getQueue("queue:users")).thenReturn(expiredUsers);

        queueService.deleteByExpiresAtBefore();

        verify(redisRepository, times(1)).removeFromQueue("queue:users", "1:WAITING:testToken");
    }
}
