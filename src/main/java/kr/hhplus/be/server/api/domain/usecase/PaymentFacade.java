package kr.hhplus.be.server.api.domain.usecase;

import kr.hhplus.be.server.domain.service.PaymentService;
import kr.hhplus.be.server.domain.service.ReservationService;
import org.springframework.stereotype.Component;

/**
 * PaymentFacade
 * - 결제와 관련된 비즈니스 로직을 캡슐화하여 제공하는 클래스.
 * - 결제와 예약 상태를 조합하여 고수준의 작업을 처리.
 */
@Component
public class PaymentFacade {

    private final PaymentService paymentService;
    private final ReservationService reservationService;

    public PaymentFacade(PaymentService paymentService, ReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    /**
     * 사용자 결제를 처리하고 예약 상태를 업데이트합니다.
     * @param userId 결제를 요청한 사용자 ID
     * @param reservationId 예약 ID
     * @throws IllegalArgumentException 결제가 이미 진행 중일 경우 예외 발생
     */
    public void processPayment(Long userId, Long reservationId) {
        // 결제 상태 확인
        if (paymentService.isPaymentPending(reservationId)) {
            throw new IllegalArgumentException("Payment is already in progress.");
        }

        // 결제 처리
        paymentService.processPayment(userId, reservationId);

        // 결제 완료 후 예약 상태 업데이트
        reservationService.updateReservationStatus(reservationId, "CONFIRMED");
    }
}
