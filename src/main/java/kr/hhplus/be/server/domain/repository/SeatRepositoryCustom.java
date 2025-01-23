package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Seat;

import java.time.LocalDate;
import java.util.List;

/**
 * SeatRepositoryCustom
 * - 커스텀 쿼리 및 복잡한 데이터 접근 로직 정의.
 */
public interface SeatRepositoryCustom {

    /**
     * 여러 스케줄 ID에 해당하는 사용 가능한 좌석 조회.
     * @param scheduleIds 스케줄 ID 리스트
     * @return 사용 가능한 좌석 리스트
     */
    List<Seat> findAvailableSeatsByScheduleIds(List<Long> scheduleIds);

    /**
     * 특정 좌석 ID의 가격 조회.
     * @param seatId 좌석 ID
     * @return 좌석 가격
     */
    Long getSeatPrice(Long seatId);

    /**
     * 좌석 상태 업데이트.
     * @param seatId 좌석 ID
     * @param reserved 예약 상태
     * @param userId 예약 사용자 ID
     */
    void updateSeatStatus(Long seatId, boolean reserved, Long userId);

    /**
     * 좌석 ID로 사용 가능한 좌석인지 확인.
     * @param seatId 좌석 ID
     * @return 사용 가능 여부
     */
    boolean isSeatAvailable(Long seatId);

    /**
     * 특정 날짜에 해당하는 스케줄 ID 조회.
     * @param scheduleDate 스케줄 날짜
     * @return 스케줄 ID 리스트
     */
    List<Long> findScheduleIdsByDate(LocalDate scheduleDate);

}
