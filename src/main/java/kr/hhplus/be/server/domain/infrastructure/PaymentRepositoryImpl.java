package kr.hhplus.be.server.domain.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.repository.PaymentRepositoryCustom;
import org.springframework.stereotype.Repository;

/**
 * PaymentRepositoryImpl
 * - 커스텀 쿼리 로직을 구현하는 클래스.
 */
@Repository
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 특정 결제가 실패 상태인지 확인.
     * @param reservationId 예약 ID
     * @return 실패 상태 여부
     */
    @Override
    public boolean isPaymentFailed(Long reservationId) {
        String query = """
            SELECT COUNT(p) > 0
            FROM Payment p
            WHERE p.reservationId.id = :reservationId
              AND p.status = 'FAILED'
        """;

        return entityManager.createQuery(query, Boolean.class)
                .setParameter("reservationId", reservationId)
                .getSingleResult();
    }


    /**
     * 특정 예약 ID가 PENDING 상태인지 확인.
     * @param reservationId 예약 ID
     * @return PENDING 상태 여부
     */
    @Override
    public boolean isPaymentPending(Long reservationId) {
        String query = """
            SELECT COUNT(p) > 0
            FROM Payment p
           WHERE p.reservationId = :reservationId
              AND p.status = 'PENDING'
        """;

        return entityManager.createQuery(query, Boolean.class)
                .setParameter("reservationId", reservationId)
                .getSingleResult();
    }

    @Override
    @Transactional
    public void updateStatusByUserId(Long userId, String status) {
        String jpql = "UPDATE Payment p SET p.status = :status WHERE p.userId = :userId";
        entityManager.createQuery(jpql)
                .setParameter("userId", userId)
                .setParameter("status", status)
                .executeUpdate();
    }

}
