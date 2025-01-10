package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Payment;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import kr.hhplus.be.server.domain.service.PaymentService;
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
public class PaymentServiceIntegrationTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatRepository seatRepository;

    @Test
    @Transactional
    public void testProcessPayment() {
        Reservation reservation = new Reservation();
        reservation.setSeatId(1L);
        reservationRepository.save(reservation);

        paymentService.processPayment(1L, reservation.getId());

        Payment payment = paymentRepository.findByReservationId(reservation.getId()).orElse(null);
        assertNotNull(payment);
        assertEquals("PENDING", payment.getStatus());
    }

    @Test
    @Transactional
    public void testIsPaymentPending() {
        Reservation reservation = new Reservation();
        reservationRepository.save(reservation);

        Payment payment = new Payment();
        payment.setReservationId(3L);
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        boolean result = paymentService.isPaymentPending(reservation.getId());

        assertTrue(result);
    }
}