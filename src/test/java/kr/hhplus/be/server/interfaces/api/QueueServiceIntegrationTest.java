package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.domain.repository.QueueRepository;
import kr.hhplus.be.server.domain.service.QueueService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class QueueServiceIntegrationTest {

    @InjectMocks
    private QueueService queueService;

    @Mock
    private QueueRepository queueRepository;

    @Test
    @Transactional
    public void testAddQueueEntry() {
        String tokenId = "test-token";

        queueService.addQueueEntry(tokenId);

        Optional<Queue> queue = queueRepository.findByTokenId(tokenId);
        assertTrue(queue.isPresent());
        assertEquals("WAITING", queue.get().getStatus());
    }

    @Test
    @Transactional
    public void testRemoveExpiredEntries() {
        // 고정된 시간 설정
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-09T21:03:44Z"), ZoneId.systemDefault());
        LocalDateTime fixedTime = LocalDateTime.now(fixedClock);
        Queue queue = new Queue();
        queue.setTokenId("test-token");
        queue.setStatus("WAITING");
        queue.setExpiresAt(fixedTime.minusMinutes(1));
        queueRepository.save(queue);

        queueService.removeExpiredEntries();

        Optional<Queue> result = queueRepository.findByTokenId("test-token");
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    public void testFindQueueEntryByTokenId() {
        String tokenId = "test-token";
        Queue queue = new Queue();
        queue.setTokenId(tokenId);
        queueRepository.save(queue);

        Optional<Queue> result = queueService.findQueueEntryByTokenId(tokenId);

        assertTrue(result.isPresent());
        assertEquals(queue, result.get());
    }
}