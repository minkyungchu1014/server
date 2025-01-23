package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Payment;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.service.PaymentService;
import kr.hhplus.be.server.domain.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository; // Mock 추가

    @Mock
    private SeatRepository seatRepository; // Mock 추가

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PointService pointService; // Mock 추가

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long seatId = 100L;
        Long seatPrice = 500L;
        Long userPoints = 1000L;

        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        mockReservation.setSeatId(seatId);

        Payment mockPayment = new Payment();
        mockPayment.setReservationId(reservationId);
        mockPayment.setStatus("PENDING");

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(seatRepository.getSeatPrice(seatId)).thenReturn(seatPrice); // seatPrice 반환값 설정
        when(pointService.getPoint(userId)).thenReturn(userPoints);
        when(paymentRepository.findByReservationId(reservationId)).thenReturn(Optional.of(mockPayment));

        // Act
        paymentService.processPayment(userId, reservationId);

        // Assert
        verify(paymentRepository, times(1)).save(mockPayment);
        assertEquals("COMPLETE", mockPayment.getStatus());
    }

    @Test
    void testProcessPayment_InsufficientPoints() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long seatId = 100L;
        Long seatPrice = 500L;
        Long userPoints = 300L;

        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        mockReservation.setSeatId(seatId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(seatRepository.getSeatPrice(seatId)).thenReturn(seatPrice); // seatPrice 반환값 설정
        when(pointService.getPoint(userId)).thenReturn(userPoints);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(userId, reservationId));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
