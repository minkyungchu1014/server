package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.service.ReservationService;
import kr.hhplus.be.server.domain.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatService seatService;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private RedisRepository redisRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCancelReservation_Success() {
        Long reservationId = 1L;

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setSeatId(1L);
        reservation.setStatus("RESERVED");

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(reservationId);

        assertThat(reservation.getStatus()).isEqualTo("CANCELLED");
        verify(seatService).markSeatAsAvailable(reservation.getSeatId());
        verify(reservationRepository).save(reservation);
        verify(redisRepository).removeFromQueue(anyString(), anyString());
    }
}
