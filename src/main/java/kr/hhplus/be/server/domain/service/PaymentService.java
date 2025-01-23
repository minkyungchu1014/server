package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.models.Payment;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final PointService pointService;
public PaymentService(PaymentRepository paymentRepository, ReservationRepository reservationRepository, SeatRepository seatRepository, PointService pointService) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.pointService = pointService;
    }

    public Payment insertPaymentStatus(Long reservationId, Long price, Long userId) {
        Optional<Payment> check = paymentRepository.findByReservationId(reservationId);
        if (check.isPresent()){
            throw new IllegalArgumentException("이미 결재 정보가 존재합니다.");
        }

        Payment payment = new Payment();
        payment.setReservationId(reservationId);
        payment.setUserId(userId);
        payment.setAmount(price); //Seat 테이블에서 가격을 가져와야함
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        return payment;
    }

    /**
     * 결제를 처리하고 데이터베이스에 저장합니다.
     * @param userId 사용자 ID
     * @param reservationId 예약 ID
     */
    public void processPayment(Long userId, Long reservationId) {
        // 예약 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약 ID로 예약된 정보가 없습니다."));

        // 좌석의 가격 정보 가져오기
        Long seatId = reservation.getSeatId();
        Long seatPrice = seatRepository.getSeatPrice(seatId);

        // 사용자 포인트 확인 및 차감
        Long userPoint = pointService.getPoint(userId);
        if (userPoint < seatPrice) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // Payment 상태 변경
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("결제관련 정보가 없습니다."));
        payment.setStatus("COMPLETE");
        
        // 데이터베이스에 저장
        paymentRepository.save(payment);
    }

    public boolean isPaymentPending(Long reservationId) {
        return paymentRepository.isPaymentPending(reservationId);
    }

}
