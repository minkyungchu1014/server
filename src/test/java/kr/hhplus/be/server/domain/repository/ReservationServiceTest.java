package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.service.ReservationService;
import kr.hhplus.be.server.domain.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCancelReservation_Success() {
        // Arrange
        Long reservationId = 1L;

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setSeatId(1L);
        reservation.setStatus("RESERVED");

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act
        reservationService.cancelReservation(reservationId);

        // Assert
        assertThat(reservation.getStatus()).isEqualTo("CANCELLED");
        verify(seatService).markSeatAsAvailable(reservation.getSeatId());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void testProcessExpiredReservations() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setSeatId(1L);
        reservation.setStatus("PENDING");
        reservation.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(reservationRepository.findByStatusAndExpiresAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(List.of(reservation));

        // Act
        reservationService.processExpiredReservations();

        // Assert
        assertThat(reservation.getStatus()).isEqualTo("EXPIRED"); // 상태 업데이트 확인
        verify(seatService).markSeatAsAvailable(reservation.getSeatId()); // 좌석 상태 업데이트 확인
        verify(reservationRepository).save(reservation); // 예약 상태 저장 확인
    }

    @Test
    void testReserveSeat_Success() {
        // Arrange
        Long userId = 1L;
        Long seatId = 1L;

        when(reservationRepository.findReservationWithLock(seatId)).thenReturn(Optional.empty());

        // Act
        Reservation reservation = reservationService.reserveSeat(userId, seatId);

        // Assert
        assertThat(reservation).isNotNull();
        assertThat(reservation.getUserId()).isEqualTo(userId);
        assertThat(reservation.getSeatId()).isEqualTo(seatId);
        assertThat(reservation.getStatus()).isEqualTo("RESERVED");
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void testReserveSeat_Failure_AlreadyReserved() {
        // Arrange
        Long userId = 1L;
        Long seatId = 1L;

        Reservation existingReservation = new Reservation();
        existingReservation.setSeatId(seatId);
        existingReservation.setStatus("RESERVED");

        when(reservationRepository.findReservationWithLock(seatId)).thenReturn(Optional.of(existingReservation));

        // Act & Assert
        try {
            reservationService.reserveSeat(userId, seatId);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("이미 예약된 좌석입니다.");
        }

        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    @Test
    void testCancelReservation_Failure_AlreadyCancelled() {
        // Arrange
        Long reservationId = 1L;

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setSeatId(1L);
        reservation.setStatus("CANCELLED");

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act & Assert
        try {
            reservationService.cancelReservation(reservationId);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("이미 취소된 예약입니다.");
        }

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

}
