package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import kr.hhplus.be.server.domain.service.SeatService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class SeatServiceIntegrationTest {

    @InjectMocks
    private SeatService seatService;

    @Mock
    private SeatRepository seatRepository;

    @Test
    @Transactional
    public void testIsSeatAvailable() {
        Seat seat = new Seat();
        seat.setIsReserved(false);
        seatRepository.save(seat);

        boolean result = seatService.isSeatAvailable(seat.getId());

        assertTrue(result);
    }

    @Test
    @Transactional
    public void testGetSeatPrice() {
        Seat seat = new Seat();
        seat.setPrice(100L);
        seatRepository.save(seat);

        Long price = seatService.getSeatPrice(seat.getId());

        assertEquals(100L, price);
    }

    @Test
    @Transactional
    public void testMarkSeatAsReserved() {
        Seat seat = new Seat();
        seat.setIsReserved(false);
        seatRepository.save(seat);

        seatService.markSeatAsReserved(seat.getId(), 1L);

        Seat updatedSeat = seatRepository.findById(seat.getId()).orElse(null);
        assertNotNull(updatedSeat);
        assertTrue(updatedSeat.getIsReserved());
        assertEquals(1L, updatedSeat.getReservedBy());
    }

    @Test
    @Transactional
    public void testMarkSeatAsAvailable() {
        Seat seat = new Seat();
        seat.setIsReserved(true);
        seat.setReservedBy(1L);
        seatRepository.save(seat);

        seatService.markSeatAsAvailable(seat.getId());

        Seat updatedSeat = seatRepository.findById(seat.getId()).orElse(null);
        assertNotNull(updatedSeat);
        assertFalse(updatedSeat.getIsReserved());
        assertNull(updatedSeat.getReservedBy());
    }
}