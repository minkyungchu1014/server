package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.domain.repository.QueueRepository;
import kr.hhplus.be.server.domain.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class QueueServiceTest {

    @Mock
    private QueueRepository queueRepository;

    @InjectMocks
    private QueueService queueService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddQueueEntry() {
        String tokenId = "test-token";

        queueService.addQueueEntry(tokenId);

        verify(queueRepository, times(1)).save(any(Queue.class));
    }

    @Test
    public void testRemoveExpiredEntries() {
        queueService.removeExpiredEntries();

        verify(queueRepository, times(1)).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    public void testFindQueueEntryByTokenId() {
        String tokenId = "test-token";
        Queue queue = new Queue();
        when(queueRepository.findByTokenId(tokenId)).thenReturn(Optional.of(queue));

        Optional<Queue> result = queueService.findQueueEntryByTokenId(tokenId);

        assertTrue(result.isPresent());
        assertEquals(queue, result.get());
    }
}