package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.service.ExpirationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;

@Testcontainers
@ActiveProfiles("test")
public class ExpirationServiceTest {

    @Mock
    private QueueRepository queueRepository;

    @InjectMocks
    private ExpirationService expirationService;

    @Mock
    private ReservationRepository reservationRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testProcessExpiredReservationsAndTokens() {
        // Arrange
        LocalDateTime currentTime = LocalDateTime.now();

        when(queueRepository.findByStatusAndExpiresAtBefore(eq("ACTIVE"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        expirationService.processExpiredReservationsAndTokens();

        // Assert
        verify(queueRepository).findByStatusAndExpiresAtBefore(eq("ACTIVE"), any(LocalDateTime.class));
        verifyNoMoreInteractions(queueRepository);
    }

}
