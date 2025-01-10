package kr.hhplus.be.server.domain.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.repository.SeatRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * SeatRepositoryImpl
 * - 커스텀 로직을 구현한 클래스.
 */
@Repository
public class SeatRepositoryImpl implements SeatRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 여러 스케줄 ID에 해당하는 사용 가능한 좌석 조회.
     * @param scheduleIds 스케줄 ID 리스트
     * @return 사용 가능한 좌석 리스트
     */
    @Override
    public List<Seat> findAvailableSeatsByScheduleIds(List<Long> scheduleIds) {
        String query = """
            SELECT s
            FROM Seat s
            WHERE s.concertScheduleId IN :scheduleIds
              AND s.isReserved = false
        """;

        return entityManager.createQuery(query, Seat.class)
                .setParameter("scheduleIds", scheduleIds)
                .getResultList();
    }

    /**
     * 특정 좌석 ID의 가격 조회.
     * @param seatId 좌석 ID
     * @return 좌석 가격
     */
    @Override
    public Long getSeatPrice(Long seatId) {
        String query = """
            SELECT s.price
            FROM Seat s
            WHERE s.id = :seatId
        """;

        return entityManager.createQuery(query, Long.class)
                .setParameter("seatId", seatId)
                .getSingleResult();
    }

    /**
     * 좌석 상태 업데이트.
     * @param seatId 좌석 ID
     * @param reserved 예약 상태
     * @param userId 예약 사용자 ID
     */
    @Override
    public void updateSeatStatus(Long seatId, boolean reserved, Long userId) {
        String query = """
            UPDATE Seat s
            SET s.isReserved = :reserved,
                s.reservedBy = :userId
            WHERE s.id = :seatId
        """;

        entityManager.createQuery(query)
                .setParameter("reserved", reserved)
                .setParameter("userId", userId)
                .setParameter("seatId", seatId)
                .executeUpdate();
    }

    /**
     * 좌석 ID로 사용 가능한 좌석인지 확인.
     * @param seatId 좌석 ID
     * @return 사용 가능 여부
     */
    @Override
    public boolean isSeatAvailable(Long seatId) {
        String query = """
            SELECT COUNT(s) > 0
            FROM Seat s
            WHERE s.id = :seatId
              AND s.isReserved = false
        """;

        return entityManager.createQuery(query, Boolean.class)
                .setParameter("seatId", seatId)
                .getSingleResult();
    }

    @Override
    public List<Long> findScheduleIdsByDate(LocalDate scheduleDate) {
        String query = """
            SELECT cs.id
            FROM ConcertSchedule cs
            WHERE cs.scheduleDate = :scheduleDate
        """;

        return entityManager.createQuery(query, Long.class)
                .setParameter("scheduleDate", scheduleDate)
                .getResultList();
    }

}
