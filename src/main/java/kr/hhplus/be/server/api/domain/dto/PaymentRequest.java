package kr.hhplus.be.server.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Long userId;
    private Long reservationId;

    public PaymentRequest(Long userId, Long reservationId) {
        this.userId = userId;
        this.reservationId = reservationId;
    }
}