package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.domain.repository.*;
import kr.hhplus.be.server.domain.service.ExpirationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class ExpirationServiceIntegrationTest {

    @InjectMocks
    private ExpirationService expirationService;

    @Mock
    private QueueRepository queueRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SeatRepository seatRepository;

    @Test
    @Transactional
    public void testProcessExpiredReservationsAndTokens() {
        // 고정된 시간 설정
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-09T21:03:44Z"), ZoneId.systemDefault());
        LocalDateTime fixedTime = LocalDateTime.now(fixedClock);
        Queue queue = new Queue();
        queue.setUserId(1L);
        queue.setTokenId("test-token");
        queue.setStatus("ACTIVE");
        queue.setExpiresAt(fixedTime.minusMinutes(1));
        queueRepository.save(queue);

        expirationService.processExpiredReservationsAndTokens();

        List<Queue> expiredTokens = queueRepository.findByStatusAndExpiresAtBefore("ACTIVE", fixedTime);
        assertTrue(expiredTokens.isEmpty());
    }
}