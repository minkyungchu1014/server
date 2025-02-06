package kr.hhplus.be.server.domain.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.repository.ReservationRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ReservationRepositoryImpl
 * - 커스텀 로직을 구현한 클래스.
 */
@Repository
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    /**
     * 예약 취소.
     * @param reservationId 취소할 예약 ID
     */
    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = entityManager.find(Reservation.class, reservationId);
        if (reservation != null) {
            entityManager.remove(reservation);
        }
    }
    /**
     * 예약 가능한 날짜를 조회합니다.
     * 예약 상태가 "AVAILABLE"이고 만료일이 현재 이후인 데이터를 조회합니다.
     * @return 예약 가능한 날짜 리스트
     */
    @Override
    public List<LocalDate> findAvailableDates() {
        String query = """
            SELECT DISTINCT DATE(r.expiresAt)
            FROM Reservation r
            WHERE r.status = 'AVAILABLE'
              AND r.expiresAt > CURRENT_DATE
        """;

        return entityManager.createQuery(query, LocalDate.class)
                .getResultList();
    }

    /**
     * 특정 예약의 상태를 업데이트합니다.
     * @param reservationId 예약 ID
     * @param status 새로운 상태
     */
    @Override
    public void updateReservationStatus(Long reservationId, String status) {
        String query = """
            UPDATE Reservation r
            SET r.status = :status,
                r.updatedAt = CURRENT_TIMESTAMP
            WHERE r.id = :reservationId
        """;

        entityManager.createQuery(query)
                .setParameter("status", status)
                .setParameter("reservationId", reservationId)
                .executeUpdate();
    }

    /**
     * 상태와 만료 조건에 맞는 예약 데이터를 조회합니다.
     * @param status 예약 상태
     * @param paymentStatus 결제 상태
     * @param currentTime 현재 시간
     * @return 조건에 맞는 예약 리스트
     */
    @Override
    public List<Reservation> findExpiredReservations(String status, String paymentStatus, LocalDateTime currentTime) {
        String query = """
        SELECT r
        FROM Reservation r
        JOIN Payment p ON r.id = p.reservationId
        JOIN Queue q ON r.userId = q.userId
        WHERE r.status = :status
          AND p.status = :paymentStatus
          AND q.expiresAt < :currentTime
    """;

        return entityManager.createQuery(query, Reservation.class)
                .setParameter("status", status)
                .setParameter("paymentStatus", paymentStatus)
                .setParameter("currentTime", currentTime)
                .getResultList();
    }

    public void updateStatusByUserId(Long userId, String status) {
        String query = """
            UPDATE Reservation r
            SET r.status = :status,
                r.updatedAt = CURRENT_TIMESTAMP
            WHERE r.userId = :userId
        """;

        entityManager.createQuery(query)
                .setParameter("status", status)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public int updateStatusById(Long id, String status){
        String query = """
            UPDATE Reservation r
            SET r.status = :status,
                r.updatedAt = CURRENT_TIMESTAMP
            WHERE r.id = :id
        """;

        return entityManager.createQuery(query)
                .setParameter("status", status)
                .setParameter("id", id)
                .executeUpdate();
    }

    public void deleteExpiredReservations(LocalDateTime currentTime) {
        String query = """
        DELETE FROM Reservation r
        WHERE r.expiresAt < :currentTime
    """;

        entityManager.createQuery(query)
                .setParameter("currentTime", currentTime)
                .executeUpdate();
    }


}
