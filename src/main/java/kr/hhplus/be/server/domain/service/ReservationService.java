package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.models.*;
import kr.hhplus.be.server.domain.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;

    public ReservationService(ReservationRepository reservationRepository, ConcertRepository concertRepository, ConcertScheduleRepository concertScheduleRepository, SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.concertRepository = concertRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.seatRepository = seatRepository;
    }

    public List<ConcertSchedule> getAvailableConcertSchedules() {
        return concertScheduleRepository.findAvailableSchedules();
    }

    public Concert getConcertById(Long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid concert ID: " + concertId));
    }

    /**
     * 특정 날짜의 예약 가능한 좌석 조회
     * @param date 조회할 날짜
     * @return 예약 가능한 좌석 리스트
     */
    public List<Seat> getAvailableSeatsByDate(String date) {
        LocalDate bsdtDate = LocalDate.parse(date);

        // 날짜에 해당하는 스케줄 ID 조회
        List<Long> scheduleIds = concertScheduleRepository.findScheduleIdsByDate(bsdtDate);

        // 해당 스케줄 ID들로 예약 가능한 좌석 조회
        return seatRepository.findAvailableSeatsByScheduleIds(scheduleIds);
    }

    /**
     * 예약 가능한 날짜를 조회합니다.
     * @return 예약 가능한 날짜 리스트
     */
    public List<String> getAvailableDates() {
        // 예약 가능한 날짜를 ReservationRepository에서 조회
        List<LocalDate> availableDates = reservationRepository.findAvailableDates();

        // 날짜 리스트를 문자열로 변환하여 반환
        return availableDates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜의 예약 가능한 좌석을 조회합니다.
     * @param date 예약 가능한 좌석을 조회할 날짜
     * @return 예약 가능한 좌석 ID 리스트
     */
    public List<Long> getAvailableSeats(String date) {
        LocalDate reservationDate = LocalDate.parse(date);

        // 특정 날짜의 예약 가능한 좌석 조회
        List<Long> availableSeats = seatRepository.findIdByDate(reservationDate);

        return availableSeats;
    }
    public void reserveSeat(Long userId, Long seatId) {
        reservationRepository.reserveSeat(userId, seatId);
    }

    public Long getSeatIdByReservation(Long reservationId) {
        return reservationRepository.findSeatIdById(reservationId);
    }

    public void updateReservationStatus(Long reservationId, String status) {
        reservationRepository.updateReservationStatus(reservationId, status);
    }

    /**
     * 만료된 예약 상태를 처리합니다.
     * - 예약 상태를 "EXPIRED"로 변경.
     * * - 좌석 상태를 비예약 상태로 변경.
     * - 대기열에서 해당 토큰 삭제.
     */
    @Transactional
    public void processExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore("PENDING", LocalDateTime.now());

        for (Reservation reservation : expiredReservations) {
            // 1. 예약 상태 변경
            reservation.setStatus("EXPIRED");
            reservation.setUpdatedAt(LocalDateTime.now());
            reservationRepository.save(reservation);

            // 2. 좌석 상태 변경
            Long seatId = reservation.getSeatId();
            seatRepository.updateSeatStatus(seatId, false, null);
        }
    }


}
