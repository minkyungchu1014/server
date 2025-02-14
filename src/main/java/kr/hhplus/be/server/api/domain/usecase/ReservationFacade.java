package kr.hhplus.be.server.api.domain.usecase;

import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.service.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationFacade {
    private final ReservationService reservationService;
    private final SeatService seatService;
    private final PaymentService paymentService;
    private final QueueService queueService;
    private final DataPlatformService dataPlatformService;

    public ReservationFacade(
            ReservationService reservationService,
            SeatService seatService,
            PaymentService paymentService,
            QueueService queueService,
            DataPlatformService dataPlatformService
    ) {
        this.reservationService = reservationService;
        this.seatService = seatService;
        this.paymentService = paymentService;
        this.queueService = queueService;
        this.dataPlatformService = dataPlatformService;
    }

    public void reserveSeat(String token, Long userId, Long seatId) {
        Reservation reservation = reservationService.reserveSeat(userId, seatId);
        Seat seat = seatService.markSeatAsReserved(seatId, userId);
        paymentService.insertPaymentStatus(reservation.getId(), seat.getPrice(), userId);
        queueService.updateQueueStatus(token, LocalDateTime.now().plusMinutes(5));

        // 데이터 플랫폼으로 예약 정보 전송
        dataPlatformService.sendReservationData(
                reservation.getId(),
                userId,
                seatId,
                seat.getConcertScheduleId()
        );
    }
}
