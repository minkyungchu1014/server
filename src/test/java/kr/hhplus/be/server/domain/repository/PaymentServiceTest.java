package kr.hhplus.be.server.domain.repository;

import static org.junit.jupiter.api.Assertions.*;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import kr.hhplus.be.server.domain.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessPayment() {
        Long userId = 1L;
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setSeatId(1L);

        when(reservationRepository.findById(reservationId)).thenReturn(java.util.Optional.of(reservation));
        when(seatRepository.getSeatPrice(1L)).thenReturn(100L);

        paymentService.processPayment(userId, reservationId);

        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    public void testIsPaymentPending() {
        Long reservationId = 1L;
        when(paymentRepository.isPaymentPending(reservationId)).thenReturn(true);

        boolean result = paymentService.isPaymentPending(reservationId);

        assertTrue(result);
    }
}