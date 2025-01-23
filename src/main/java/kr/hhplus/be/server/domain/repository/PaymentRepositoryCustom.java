package kr.hhplus.be.server.domain.repository;

import org.springframework.data.jpa.repository.Modifying;

/**
 * PaymentRepositoryCustom
 * - 커스텀 쿼리를 정의하기 위한 인터페이스.
 */
public interface PaymentRepositoryCustom {

    /**
     * 특정 결제가 실패 상태인지 확인.
     *
     * @param reservationId 예약 ID
     * @return 실패 상태 여부
     */
    boolean isPaymentFailed(Long reservationId);

    /**
     * 특정 예약 ID가 PENDING 상태인지 확인.
     *
     * @param reservationId 예약 ID
     * @return PENDING 상태 여부
     */
    boolean isPaymentPending(Long reservationId);

    void updateStatusByUserId(Long userId, String status);
}