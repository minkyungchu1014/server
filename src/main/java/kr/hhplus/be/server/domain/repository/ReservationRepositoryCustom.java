package kr.hhplus.be.server.domain.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.domain.models.Reservation;
import org.springframework.data.jpa.repository.Modifying;

/**
 * ReservationRepositoryCustom
 * - 커스텀 쿼리를 정의하는 인터페이스.
 */
public interface ReservationRepositoryCustom {

    /**
     * 사용자 ID와 좌석 ID를 기반으로 예약 생성.
     * @param userId 사용자 ID
     * @param seatId 좌석 ID
     */
    void reserveSeat(Long userId, Long seatId);

    /**
     * 예약 취소.
     * @param reservationId 취소할 예약 ID
     */
    void cancelReservation(Long reservationId);

    /**
     * 예약 가능한 날짜를 조회합니다.
     * @return 예약 가능한 날짜 리스트
     */
    List<LocalDate> findAvailableDates();

    /**
     * 특정 예약의 상태를 업데이트합니다.
     * @param reservationId 예약 ID
     * @param status 새로운 상태
     */
    void updateReservationStatus(Long reservationId, String status);

    /**
     * 상태와 만료 조건에 맞는 예약 데이터를 조회합니다.
     * @param status 예약 상태
     * @param paymentStatus 결제 상태
     * @param currentTime 현재 시간
     * @return 조건에 맞는 예약 리스트
     */
    List<Reservation> findExpiredReservations(String status, String paymentStatus, LocalDateTime currentTime);

    void updateStatusByUserId (Long userId, String status);

    @Modifying
    int updateStatusById(Long id, String status);

}
