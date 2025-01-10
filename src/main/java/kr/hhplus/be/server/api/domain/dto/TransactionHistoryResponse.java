package kr.hhplus.be.server.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionHistoryResponse {
    private Long transactionId;
    private Long amount;
    private LocalDateTime timestamp;

    public TransactionHistoryResponse(Long transactionId, Long amount, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}