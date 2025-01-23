package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public SeatService(SeatRepository seatRepository, ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    // 좌석을 예약 상태로 변경
    @Transactional
    public Seat markSeatAsReserved(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석이 존재하지 않습니다."));
        if (seat.getIsReserved()) {
            throw new IllegalArgumentException("이미 예약된 좌석입니다.");
        }

        seat.setIsReserved(true);
        seat.setReservedBy(userId);
        seatRepository.save(seat);

        return seat;
    }

    // 좌석을 비예약 상태로 변경
    @Transactional
    public void markSeatAsAvailable(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석이 존재하지 않습니다."));
        seat.setIsReserved(false);
        seat.setReservedBy(null);
        seatRepository.save(seat);
    }

    @Transactional
    public void reserveSeat(Long seatId, Long userId) {
        // 좌석을 비관적 락으로 조회
        Seat seat = seatRepository.findSeatWithLock(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석이 존재하지 않습니다."));

        // 좌석이 이미 예약된 경우 예외 처리
        if (seat.getIsReserved()) {
            throw new IllegalArgumentException("이미 예약된 좌석입니다.");
        }

        // 좌석 예약 처리
        seat.setIsReserved(true);
        seat.setReservedBy(userId);
        seatRepository.save(seat);
    }

    @Transactional
    public void processExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore("PENDING", LocalDateTime.now());

        expiredReservations.forEach(reservation -> {
            reservation.setStatus("EXPIRED");
            reservation.setUpdatedAt(LocalDateTime.now());
            reservationRepository.save(reservation);

            // 좌석 상태를 SeatService를 통해 비예약 상태로 변경
            markSeatAsAvailable(reservation.getSeatId());
        });
    }

    public boolean isSeatAvailable(Long seatId) {
        return seatRepository.isSeatAvailable(seatId);
    }

    public Long getSeatPrice(Long seatId) {
        return seatRepository.getSeatPrice(seatId);
    }

}
