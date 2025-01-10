package kr.hhplus.be.server.api.domain.controller;

import kr.hhplus.be.server.api.domain.usecase.PaymentFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PaymentController
 * - 결제와 관련된 API 요청을 처리하는 컨트롤러 클래스.
 * - 사용자 결제 처리 및 실패한 결제 재시도를 지원.
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    public PaymentController(PaymentFacade paymentFacade) {
        this.paymentFacade = paymentFacade;
    }

    /**
     * 결제 처리
     * - 사용자와 예약 정보를 기반으로 결제를 처리.
     * @param userId 사용자 ID
     * @param reservationId 예약 ID
     * @return 결제 성공 또는 실패 메시지
     */
    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestParam Long userId, @RequestParam Long reservationId) {
        try {
            paymentFacade.processPayment(userId, reservationId);
            return ResponseEntity.ok("결제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("결제 오류" + e.getMessage());
        }
    }
}
