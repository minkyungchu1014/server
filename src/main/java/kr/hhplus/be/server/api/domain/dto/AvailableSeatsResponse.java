package kr.hhplus.be.server.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 좌석 정보를 응답하는 DTO
 */
@Getter
@Setter
public class AvailableSeatsResponse {
    private Long seatId;
    private Long concertScheduleId;
    private Integer seatNumber;
    private Long price;
    private String concertName;
    private String venue;
    private String organizer;

    public AvailableSeatsResponse(Long seatId, Long concertScheduleId, Integer seatNumber, Long price, String concertName, String venue, String organizer) {
        this.seatId = seatId;
        this.concertScheduleId = concertScheduleId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.concertName = concertName;
        this.venue = venue;
        this.organizer = organizer;
    }
}