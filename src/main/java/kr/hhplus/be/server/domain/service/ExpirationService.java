package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpirationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final QueueRepository queueRepository;
    private final PaymentRepository paymentRepository;
    private final TokenRepository tokenRepository;
    public ExpirationService(ReservationRepository reservationRepository,
                             SeatRepository seatRepository,
                             QueueRepository queueRepository, PaymentRepository paymentRepository, TokenRepository tokenRepository) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.queueRepository = queueRepository;
        this.paymentRepository = paymentRepository;
        this.tokenRepository = tokenRepository;
    }

    /**
     * 만료된 예약 및 대기열 토큰을 처리합니다.
     */
    @Transactional
    public void processExpiredReservationsAndTokens() {

        LocalDateTime currentTime = LocalDateTime.now();

        // 1. 만료된 토큰 조회
        List<Queue> expiredTokens = queueRepository.findByStatusAndExpiresAtBefore("ACTIVE", currentTime);

        for (Queue token : expiredTokens) {

            // 3. 예약 상태 업데이트 (EXPIRED)
            reservationRepository.updateStatusByUserId(token.getUserId(), "EXPIRED");

            // 4. 결제 상태 업데이트 (FAILED)
            paymentRepository.updateStatusByUserId(token.getUserId(), "FAILED");

            // 5. 좌석 상태 업데이트 (isReserved = false, reservedBy = null)
            seatRepository.updateIsReservedFalseAndReservedByEmptyByUserId(token.getUserId());

            // 2. 토큰 삭제
            queueRepository.delete(token);
        }
    }
}
