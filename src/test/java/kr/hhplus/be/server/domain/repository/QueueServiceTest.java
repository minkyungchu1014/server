package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

import static org.mockito.Mockito.*;

class QueueServiceTest {

    @InjectMocks
    private QueueService queueService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void testAddToQueue() {
        String tokenId = "testToken";
        Long userId = 1L;
        String value = userId + ":WAITING:" + tokenId;
        long expiresAt = System.currentTimeMillis() + (30 * 60 * 1000);

        queueService.addToQueue(tokenId, userId);

        verify(zSetOperations, times(1)).add("queue:users", value, expiresAt);
    }

    @Test
    void testActivateTokens() {
        Set<String> mockUsers = Set.of("1:WAITING:testToken");

        when(zSetOperations.range("queue:users", 0, -1)).thenReturn(mockUsers);

        queueService.activateTokens(1);

        verify(zSetOperations, times(1)).remove("queue:users", "1:WAITING:testToken");
        verify(zSetOperations, times(1)).add(anyString(), anyString(), anyDouble());
    }

    @Test
    void testDeleteByExpiresAtBefore() {
        Set<String> expiredUsers = Set.of("1:WAITING:testToken");

        when(zSetOperations.rangeByScore("queue:users", 0, System.currentTimeMillis())).thenReturn(expiredUsers);

        queueService.deleteByExpiresAtBefore();

        verify(zSetOperations, times(1)).remove("queue:users", "1:WAITING:testToken");
    }
}
