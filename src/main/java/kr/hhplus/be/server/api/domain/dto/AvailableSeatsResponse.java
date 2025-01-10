package kr.hhplus.be.server.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 좌석 정보를 응답하는 DTO
 */
@Getter
@Setter
public class AvailableSeatsResponse {
    private Long id;
    private Long concertScheduleId;
    private Long seatNumber;

    public AvailableSeatsResponse(Long id, Integer seatNumber, Long price) {
    }
}