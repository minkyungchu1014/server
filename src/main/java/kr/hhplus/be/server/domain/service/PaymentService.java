package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.models.Payment;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public PaymentService(PaymentRepository paymentRepository, ReservationRepository reservationRepository, SeatRepository seatRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }


    /**
     * 결제를 처리하고 데이터베이스에 저장합니다.
     * @param userId 사용자 ID
     * @param reservationId 예약 ID
     */
    public void processPayment(Long userId, Long reservationId) {
        // 예약 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation ID: " + reservationId));

        // 좌석의 가격 정보 가져오기
        Long seatId = reservation.getSeatId();
        Long price = seatRepository.getSeatPrice(seatId);

        // Payment 엔티티 생성
        Payment payment = new Payment();
        payment.setReservationId(reservationId);
        payment.setUserId(userId);
        payment.setAmount(price);
        payment.setStatus("PENDING");

        // 데이터베이스에 저장
        paymentRepository.save(payment);
    }

    public boolean isPaymentPending(Long reservationId) {
        return paymentRepository.isPaymentPending(reservationId);
    }

}
