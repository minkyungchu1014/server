package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatServiceTest {

    @InjectMocks
    private SeatService seatService;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-22T00:00:00Z"), ZoneId.of("UTC"));
        LocalDateTime fixedNow = LocalDateTime.now(fixedClock);

    }

    @Test
    void testMarkSeatAsReserved_Success() {
        Long seatId = 1L;
        Long userId = 1L;
        Seat seat = new Seat();
        seat.setIsReserved(false);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        Seat result = seatService.markSeatAsReserved(seatId, userId);

        assertTrue(result.getIsReserved());
        assertEquals(userId, result.getReservedBy());
        verify(seatRepository, times(1)).findById(seatId);
        verify(seatRepository, times(1)).save(seat);
    }

    @Test
    void testMarkSeatAsReserved_AlreadyReserved() {
        Long seatId = 1L;
        Long userId = 1L;
        Seat seat = new Seat();
        seat.setIsReserved(true);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            seatService.markSeatAsReserved(seatId, userId);
        });

        assertEquals("이미 예약된 좌석입니다.", exception.getMessage());
        verify(seatRepository, times(1)).findById(seatId);
        verify(seatRepository, never()).save(seat);
    }

    @Test
    void testMarkSeatAsAvailable_Success() {
        Long seatId = 1L;
        Seat seat = new Seat();
        seat.setIsReserved(true);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        seatService.markSeatAsAvailable(seatId);

        assertFalse(seat.getIsReserved());
        assertNull(seat.getReservedBy());
        verify(seatRepository, times(1)).findById(seatId);
        verify(seatRepository, times(1)).save(seat);
    }

    @Test
    void testReserveSeat_Success() {
        Long seatId = 1L;
        Long userId = 1L;
        Seat seat = new Seat();
        seat.setIsReserved(false);
        when(seatRepository.findSeatWithLock(seatId)).thenReturn(Optional.of(seat));

        seatService.reserveSeat(seatId, userId);

        assertTrue(seat.getIsReserved());
        assertEquals(userId, seat.getReservedBy());
        verify(seatRepository, times(1)).findSeatWithLock(seatId);
        verify(seatRepository, times(1)).save(seat);
    }

    @Test
    void testReserveSeat_AlreadyReserved() {
        Long seatId = 1L;
        Long userId = 1L;
        Seat seat = new Seat();
        seat.setIsReserved(true);
        when(seatRepository.findSeatWithLock(seatId)).thenReturn(Optional.of(seat));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            seatService.reserveSeat(seatId, userId);
        });

        assertEquals("이미 예약된 좌석입니다.", exception.getMessage());
        verify(seatRepository, times(1)).findSeatWithLock(seatId);
        verify(seatRepository, never()).save(seat);
    }

    @Test
    void testProcessExpiredReservations() {
        // Arrange: 만료된 예약 및 좌석 데이터 준비
        List<Reservation> expiredReservations = List.of(
                Reservation.builder()
                        .userId(1L)
                        .seatId(101L)
                        .concertScheduleId(1001L)
                        .status("PENDING")
                        .expiresAt(LocalDateTime.now().plusMinutes(30))
                        .build()
        );

        Seat mockSeat = new Seat();
        mockSeat.setId(101L);
        mockSeat.setIsReserved(true);
        mockSeat.setReservedBy(1L);

        // Mock 설정
        when(reservationRepository.findByStatusAndExpiresAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(expiredReservations);
        when(seatRepository.findById(101L)).thenReturn(Optional.of(mockSeat));

        // Act: 만료 처리 실행
        seatService.processExpiredReservations();

        // Assert: 예약 상태 변경 확인
        verify(reservationRepository, times(1)).save(any(Reservation.class));

        // Assert: 좌석 상태 변경 확인
        verify(seatRepository, times(1)).save(mockSeat);
    }



    @Test
    void testIsSeatAvailable() {
        Long seatId = 1L;
        when(seatRepository.isSeatAvailable(seatId)).thenReturn(true);

        boolean result = seatService.isSeatAvailable(seatId);

        assertTrue(result);
        verify(seatRepository, times(1)).isSeatAvailable(seatId);
    }

    @Test
    void testGetSeatPrice() {
        Long seatId = 1L;
        Long price = 100L;
        when(seatRepository.getSeatPrice(seatId)).thenReturn(price);

        Long result = seatService.getSeatPrice(seatId);

        assertEquals(price, result);
        verify(seatRepository, times(1)).getSeatPrice(seatId);
    }
}