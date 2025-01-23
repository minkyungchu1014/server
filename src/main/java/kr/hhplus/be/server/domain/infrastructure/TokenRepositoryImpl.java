package kr.hhplus.be.server.domain.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.repository.TokenRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TokenRepositoryImpl
 * - 커스텀 로직을 구현한 클래스.
 */
@Repository
public class TokenRepositoryImpl implements TokenRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void updateStatusAndExpiresAt(String tokenId, String status, LocalDateTime expiresAt) {
        String query = """
            UPDATE Token t
            SET t.status = :status,
                t.expiresAt = :expiresAt
            WHERE t.token = :token
        """;

        entityManager.createQuery(query)
                .setParameter("status", status)
                .setParameter("expiresAt", expiresAt)
                .setParameter("token", tokenId)
                .executeUpdate();
    }

    @Override
    public boolean existsActiveTokenByUserId(Long userId) {
        String query = "SELECT COUNT(t) > 0 FROM Token t WHERE t.userId = :userId AND t.status = 'ACTIVE'";
        return entityManager.createQuery(query, Boolean.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public void updateStatusAndExpiresAtForWaitingTokens(String newStatus, LocalDateTime expiresAt) {
        String query = """
            UPDATE Token t
            SET t.status = :newStatus,
                t.expiresAt = :expiresAt
            WHERE t.status = 'ACTIVE'
              AND t.expiresAt > :now
        """;

        entityManager.createQuery(query)
                .setParameter("newStatus", newStatus)
                .setParameter("expiresAt", expiresAt)
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
    }

    public void updateTokenStatus(String tokenId, String status) {
        String query = """
            UPDATE Token t
            SET t.status = :status
            WHERE t.token = :token
        """;

        entityManager.createQuery(query)
                .setParameter("status", status)
                .setParameter("token", tokenId)
                .executeUpdate();
    }

    public List<String> findTokensByStatusWaiting(int limit) {
        String query = """
            SELECT t.token
            FROM Token t
            WHERE t.status = 'WAITING'
              AND t.expiresAt > :now
            ORDER BY t.expiresAt ASC
        """;

        return entityManager.createQuery(query, String.class)
                .setParameter("now", LocalDateTime.now())
                .setMaxResults(limit)
                .getResultList();
    }
}
