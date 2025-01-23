package kr.hhplus.be.server.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 예약 가능 날짜 및 콘서트 정보를 응답하는 DTO
 */
@Getter
@Setter
public class AvailableDateResponse {
    private Long id;
    private Long concertId;
    private String name;
    private LocalDateTime date;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String venue;
    private String organizer;

    public AvailableDateResponse(Long id, Long concertId, String name, LocalDateTime date, String venue, String organizer, LocalDateTime startDatetime, LocalDateTime endDatetime) {
        this.id = id;
        this.concertId = concertId;
        this.name = name;
        this.date = date;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.venue = venue;
        this.organizer = organizer;
    }
}