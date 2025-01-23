package kr.hhplus.be.server.domain.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.repository.QueueRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * QueueRepositoryImpl
 * - 커스텀 로직을 구현한 클래스.
 */
@Repository
public class QueueRepositoryImpl implements QueueRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 특정 토큰의 대기열 순서를 반환.
     * WAITING 상태이고 해당 토큰 생성 시간 이전의 항목 수를 계산.
     * @param tokenId 토큰 ID
     * @return 대기열 순서
     */
    @Override
    public long getQueueOrderByToken(String tokenId) {
        String query = """
            SELECT COUNT(q)
            FROM Queue q
            WHERE q.status = 'WAITING'
              AND q.createdAt < (
                  SELECT q2.createdAt
                  FROM Queue q2
                  WHERE q2.tokenId = :tokenId
              )
        """;

        Long count = entityManager.createQuery(query, Long.class)
                .setParameter("tokenId", tokenId)
                .getSingleResult();

        return count == null ? 0L : count;
    }

    public int countTokensByStatus(String status) {
        String query = """
            SELECT COUNT(q)
            FROM Queue q
            WHERE q.status = :status
        """;

        return entityManager.createQuery(query, Integer.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Override
    public long getQueuePosition(String tokenId) {
        String query = """
            SELECT COUNT(q)
            FROM Queue q 
            WHERE q.status = 'WAITING' 
            AND q.createdAt < (SELECT q2.createdAt FROM Queue q2 WHERE q2.tokenId = :tokenId)
            """;

        return entityManager.createQuery(query, Long.class)
                .setParameter("tokenId", tokenId)
                .getSingleResult();
    }

    @Override
    public void updateExpiresAt(String token, LocalDateTime expiresAt) {
        String query = """
            UPDATE Queue q
            SET  q.expiresAt = :expiresAt
            WHERE q.tokenId = :token
        """;

        entityManager.createQuery(query)
                .setParameter("expiresAt", expiresAt)
                .setParameter("token", token)
                .executeUpdate();
    }
}
