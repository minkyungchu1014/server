package kr.hhplus.be.server.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointResponse {
    private String message;
    private Long balance;

    public PointResponse(String message) {
        this.message = message;
    }

    public PointResponse(String message, Long balance) {
        this.message = message;
        this.balance = balance;
    }

    // Getter 추가
    public String getMessage() {
        return message;
    }

    public Long getBalance() {
        return balance;
    }
}
