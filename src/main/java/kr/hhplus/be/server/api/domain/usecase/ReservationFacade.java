package kr.hhplus.be.server.api.domain.usecase;

import kr.hhplus.be.server.domain.models.*;
import kr.hhplus.be.server.api.domain.dto.*;
import kr.hhplus.be.server.domain.service.ReservationService;
import kr.hhplus.be.server.domain.service.PointService;
import kr.hhplus.be.server.domain.service.SeatService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReservationFacade
 * - 예약과 관련된 비즈니스 로직을 캡슐화하여 제공하는 클래스.
 * - 좌석 상태 및 포인트를 조합하여 예약 관리 작업을 처리.
 */
@Component
public class ReservationFacade {

    private final ReservationService reservationService;
    private final PointService pointService;
    private final SeatService seatService;

    public ReservationFacade(ReservationService reservationService, PointService pointService, SeatService seatService) {
        this.reservationService = reservationService;
        this.pointService = pointService;
        this.seatService = seatService;
    }

    public List<AvailableDateResponse> getAvailableDates() {
        return reservationService.getAvailableConcertSchedules().stream()
                .map(schedule -> {
                    Concert concert = reservationService.getConcertById(schedule.getConcertId());
                    return new AvailableDateResponse(
                            concert.getId(),
                            concert.getName(),
                            schedule.getScheduleDate().atStartOfDay(),
                            concert.getVenue(),
                            concert.getOrganizer(),
                            schedule.getStartDatetime(),
                            schedule.getEndDatetime());
                })
                .collect(Collectors.toList());
    }

    public List<AvailableSeatsResponse> getAvailableSeatsByDate(String date) {
        return reservationService.getAvailableSeatsByDate(date).stream()
                .map(seat -> new AvailableSeatsResponse(
                        seat.getId(),
                        seat.getSeatNumber(),
                        seat.getPrice()))
                .collect(Collectors.toList());
    }

    /**
     * 좌석 예약을 처리합니다.
     * @param userId 예약을 요청한 사용자 ID
     * @param seatId 예약할 좌석 ID
     * @throws IllegalArgumentException 좌석이 이미 예약된 경우 또는 포인트가 부족한 경우 예외 발생
     */
    public void reserveSeat(Long userId, Long seatId) {
        // 좌석 예약 가능 여부 확인
        if (!seatService.isSeatAvailable(seatId)) {
            throw new IllegalArgumentException("Seat is not available.");
        }

        // 좌석 가격 확인
        Long seatPrice = seatService.getSeatPrice(seatId);

        // 사용자 포인트 확인 및 차감
        Long userPoint = pointService.getPoint(userId);
        if (userPoint < seatPrice) {
            throw new IllegalArgumentException("포인트 부족");
        }
        pointService.deductPoint(userId, seatPrice, "좌석 예약금");

        // 좌석 예약 처리
        reservationService.reserveSeat(userId, seatId);

        // 좌석 상태 업데이트
        seatService.markSeatAsReserved(seatId, userId);
    }


}
