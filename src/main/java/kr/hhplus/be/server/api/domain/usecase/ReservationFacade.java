package kr.hhplus.be.server.api.domain.usecase;

import kr.hhplus.be.server.api.domain.dto.AvailableDateResponse;
import kr.hhplus.be.server.api.domain.dto.AvailableSeatsResponse;
import kr.hhplus.be.server.api.domain.interceptor.LoggingAspect;
import kr.hhplus.be.server.domain.models.Concert;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReservationFacade
 * - 예약과 관련된 비즈니스 로직을 캡슐화하여 제공하는 클래스.
 * - 좌석 상태 및 포인트를 조합하여 예약 관리 작업을 처리.
 */

@Component
public class ReservationFacade {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    private final ReservationService reservationService;
    private final SeatService seatService;
    private final ConcertService concertService;
    private final PaymentService paymentService;
    private final QueueService queueService;

    public ReservationFacade(ReservationService reservationService, SeatService seatService, ConcertService concertService, PaymentService paymentService, QueueService queueService) {
        this.reservationService = reservationService;
        this.seatService = seatService;
        this.concertService = concertService;
        this.paymentService = paymentService;
        this.queueService = queueService;
    }

    public List<AvailableDateResponse> getAvailableDates() {
        logger.info("reservationService.getAvailableConcertSchedules()", reservationService.getAvailableConcertSchedules());
        return reservationService.getAvailableConcertSchedules().stream()
                .map(schedule -> {
                    Concert concert = reservationService.getConcertById(schedule.getConcertId());
                    logger.info("reservationService.getConcertById(schedule.getConcertId())", reservationService.getConcertById(schedule.getConcertId()));
                    return new AvailableDateResponse(
                            schedule.getId(),
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
                .map(seat -> {
                    // concertScheduleId로 Concert 조회
                    Concert concert = concertService.getConcertByScheduleId(seat.getConcertScheduleId());

                    // AvailableSeatsResponse 생성
                    return new AvailableSeatsResponse(
                            seat.getId(),
                            seat.getConcertScheduleId(),
                            seat.getSeatNumber(),
                            seat.getPrice(),
                            concert.getName(),
                            concert.getVenue(),
                            concert.getOrganizer()
                    );
                }).collect(Collectors.toList());
    }

    /**
     * 좌석 예약을 처리합니다.
     * @param userId 예약을 요청한 사용자 ID
     * @param seatId 예약할 좌석 ID
     * @throws IllegalArgumentException 좌석이 이미 예약된 경우 또는 포인트가 부족한 경우 예외 발생
     */
    public void reserveSeat(String token, Long userId, Long seatId) {
        // 좌석 예약 가능 여부 확인

        // 예약 상태 확인 및 업데이트 (status -> RESERVED)
        Reservation reservation = reservationService.reserveSeat(userId, seatId);

        // 좌석 상태 확인 및 업데이트 (is_reserved, reserved_by)
        Seat seat = seatService.markSeatAsReserved(seatId, userId);

        //결제로우 하나 생성
        // 결제 상태 확인 및 업데이트 (status -> PENDING 결재 기다리는 중)
        paymentService.insertPaymentStatus(reservation.getId(), seat.getPrice(), userId);

        // 대기열 만료시간 확인 및 업데이트
        queueService.updateQueueStatus(token, LocalDateTime.now().plusMinutes(5));

    }


}
