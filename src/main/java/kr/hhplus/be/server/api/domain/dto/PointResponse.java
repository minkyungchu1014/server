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

    public PointResponse(Long balance) {
        this.balance = balance;
    }
}