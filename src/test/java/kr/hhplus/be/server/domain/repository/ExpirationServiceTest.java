package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.repository.*;
import kr.hhplus.be.server.domain.service.ExpirationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

import static org.mockito.Mockito.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class ExpirationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private QueueRepository queueRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private ExpirationService expirationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessExpiredReservationsAndTokens() {
        // 고정된 시간 설정
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-09T21:03:44Z"), ZoneId.systemDefault());
        LocalDateTime fixedTime = LocalDateTime.now(fixedClock);

        // Mock 설정
        when(queueRepository.findByStatusAndExpiresAtBefore(eq("ACTIVE"), eq(fixedTime)))
                .thenReturn(Collections.emptyList());

        // 테스트 실행
        expirationService.processExpiredReservationsAndTokens();

        // 검증
        verify(queueRepository).findByStatusAndExpiresAtBefore(eq("ACTIVE"), eq(fixedTime));
        verifyNoMoreInteractions(queueRepository);
    }
}