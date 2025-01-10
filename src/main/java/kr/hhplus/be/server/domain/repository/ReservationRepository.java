package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ReservationRepository
 * - JPA를 사용한 예약 데이터 관리.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

    /**
     * 특정 좌석 ID로 예약이 존재하는지 확인.
     * @param seatId 좌석 ID
     * @return 예약 존재 여부
     */
    boolean existsBySeatId(Long seatId);

    /**
     * 예약 ID로 좌석 ID를 조회.
     * @param reservationId 예약 ID
     * @return 좌석 ID
     */
    Long findSeatIdById(Long reservationId);

    /**
     * 예약 가능한 날짜를 가져오기 위한 기본 데이터 조회.
     * 예약 상태가 "AVAILABLE"이고 날짜가 현재 이후인 데이터를 조회합니다.
     * @return 예약 가능한 날짜 리스트
     */
    List<LocalDate> findDistinctByStatusAndExpiresAtAfter(String status, LocalDate currentDate);

    /**
     * 현재 시간보다 만료된 예약 ID를 조회합니다.
     * @param currentTime 현재 시간
     * @return 만료된 예약 ID 리스트
     */
    List<Reservation> findByStatusAndExpiresAtBefore(String status, LocalDateTime currentTime);
    /**
     * 특정 예약 ID로 좌석 ID를 조회합니다.
     *
     * @param id 예약 ID
     * @return 좌석 ID
     */
    Optional<Reservation> findById(Long id);
    /**
     * 예약 상태를 업데이트합니다.
     * @param id 예약 ID
     * @param status 새로운 상태
     * @return 업데이트된 레코드 수
     */

}
