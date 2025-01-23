package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.api.domain.dto.QueueStatusResponse;
import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.domain.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QueueServiceTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("minkyungchu")
            .withPassword("password1!");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @InjectMocks
    private QueueService queueService;

    @Mock
    private QueueRepository queueRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActivateTokens() {
        int maxActiveTokens = 5;
        when(queueRepository.countTokensByStatus("ACTIVE")).thenReturn(2);
        when(queueRepository.findTokensByStatusWaiting((Pageable) PageRequest.of(0, 3))).thenReturn(List.of("token1", "token2", "token3"));

        queueService.activateTokens(maxActiveTokens);

        verify(queueRepository, times(1)).countTokensByStatus("ACTIVE");
        verify(queueRepository, times(1)).findTokensByStatusWaiting((Pageable) PageRequest.of(0, 3));
        verify(queueRepository, times(3)).updateStatusAndExpiresAtForWaitingTokens(anyString(), eq("ACTIVE"), any(LocalDateTime.class));
    }

    @Test
    void testGetQueueStatusByToken() {
        String tokenId = "testToken";
        Queue queue = new Queue();
        queue.setTokenId(tokenId);
        queue.setStatus("WAITING");
        queue.setExpiresAt(LocalDateTime.now());
        when(queueRepository.findByTokenId(tokenId)).thenReturn(Optional.of(queue));
        when(queueRepository.getQueuePosition(tokenId)).thenReturn(1L);

        QueueStatusResponse response = queueService.getQueueStatusByToken(tokenId);

        assertEquals(tokenId, response.getTokenId());
        assertEquals("WAITING", response.getStatus());
        assertNotNull(response.getExpiresAt());
        assertEquals(1L, response.getQueuePosition());
        verify(queueRepository, times(1)).findByTokenId(tokenId);
        verify(queueRepository, times(1)).getQueuePosition(tokenId);
    }

    @Test
    void testDeleteByExpiresAtBefore() {
        queueService.deleteByExpiresAtBefore();

        verify(queueRepository, times(1)).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    void testAddToQueue() {
        String tokenId = "testToken";
        Long userId = 1L;
        when(queueRepository.existsByTokenId(tokenId)).thenReturn(false);

        queueService.addToQueue(tokenId, userId);

        verify(queueRepository, times(1)).existsByTokenId(tokenId);
        verify(queueRepository, times(1)).save(any(Queue.class));
    }

    @Test
    void testUpdateQueueStatus() {
        String token = "testToken";
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        queueService.updateQueueStatus(token, expiresAt);

        verify(queueRepository, times(1)).updateExpiresAt(token, expiresAt);
    }

    @Test
    void testIsQueueActive() {
        String token = "testToken";
        when(queueRepository.existsByTokenIdAndStatus(token, "ACTIVE")).thenReturn(true);

        boolean isActive = queueService.isQueueActive(token);

        assertTrue(isActive);
        verify(queueRepository, times(1)).existsByTokenIdAndStatus(token, "ACTIVE");
    }
}