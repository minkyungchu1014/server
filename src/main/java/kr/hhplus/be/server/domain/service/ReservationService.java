package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.api.domain.interceptor.LoggingAspect;
import kr.hhplus.be.server.domain.models.Concert;
import kr.hhplus.be.server.domain.models.ConcertSchedule;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.repository.ConcertRepository;
import kr.hhplus.be.server.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    private final ReservationRepository reservationRepository;
    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatService seatService;

    public ReservationService(ReservationRepository reservationRepository, ConcertRepository concertRepository, ConcertScheduleRepository concertScheduleRepository, SeatRepository seatRepository, SeatService seatService) {
        this.reservationRepository = reservationRepository;
        this.concertRepository = concertRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.seatRepository = seatRepository;
        this.seatService = seatService;
    }


    /**
     * 좌석을 예약합니다.
     * @param userId 사용자 ID
     * @param seatId 좌석 ID
     */
    @Transactional
    public Reservation reserveSeat(Long userId, Long seatId) {

        // 1. 예약 상태 확인
        Optional<Reservation> existingReservation = reservationRepository.findReservationWithLock(seatId);

        if (existingReservation.isPresent() &&
                ("RESERVED".equals(existingReservation.get().getStatus()) || "CONFIRMED".equals(existingReservation.get().getStatus()))) {
            throw new IllegalArgumentException("이미 예약된 좌석입니다.");
        }

        // 예약 객체 생성 및 저장
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeatId(seatId);
        reservation.setStatus("RESERVED");
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        reservationRepository.save(reservation);

        return reservation;
    }


    // 예약 취소 처리
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }

        reservation.setStatus("CANCELLED");
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        seatService.markSeatAsAvailable(reservation.getSeatId());
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
    // 특정 날짜의 예약 가능한 좌석 ID 리스트 조회
    //public List<Long> getAvailableSeats(String date) {
    //    LocalDate reservationDate = LocalDate.parse(date);
    //    return seatRepository.findIdByDate(reservationDate);
    //}

    // 좌석 예약 여부 확인
    public boolean isSeatReserved(Long seatId) {
        return seatRepository.isSeatAvailable(seatId);
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


    public Long getSeatIdByReservation(Long reservationId) {
        return reservationRepository.findSeatIdById(reservationId);
    }

    // 예약 만료 처리
    @Transactional
    public void processExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore("PENDING", LocalDateTime.now());

        expiredReservations.forEach(reservation -> {
            reservation.setStatus("EXPIRED");
            reservationRepository.save(reservation);

            seatService.markSeatAsAvailable(reservation.getSeatId());
        });
    }

    /**
     * 예약 상태를 업데이트합니다.
     *
     * @param reservationId 예약 ID
     * @param status        업데이트할 상태 (예: "CONFIRMED", "CANCELLED")
     */
    @Transactional
    public void updateReservationStatus(Long reservationId, String status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다. ID: " + reservationId));

        // 예약 상태 업데이트
        reservation.setStatus(status);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);
    }


    public Optional<Reservation> findReservationBySeatId(Long seatId) {
        return reservationRepository.findBySeatId(seatId);
    }
}
