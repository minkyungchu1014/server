package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.repository.SeatRepository;
import kr.hhplus.be.server.domain.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatService seatService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIsSeatAvailable() {
        Long seatId = 1L;
        when(seatRepository.isSeatAvailable(seatId)).thenReturn(true);

        boolean result = seatService.isSeatAvailable(seatId);

        assertTrue(result);
    }

    @Test
    public void testGetSeatPrice() {
        Long seatId = 1L;
        when(seatRepository.getSeatPrice(seatId)).thenReturn(100L);

        Long price = seatService.getSeatPrice(seatId);

        assertEquals(100L, price);
    }

    @Test
    public void testMarkSeatAsReserved() {
        Long seatId = 1L;
        Long userId = 1L;

        seatService.markSeatAsReserved(seatId, userId);

        verify(seatRepository, times(1)).updateSeatStatus(seatId, true, userId);
    }

    @Test
    public void testMarkSeatAsAvailable() {
        Long seatId = 1L;

        seatService.markSeatAsAvailable(seatId);

        verify(seatRepository, times(1)).updateSeatStatus(seatId, false, null);
    }
}