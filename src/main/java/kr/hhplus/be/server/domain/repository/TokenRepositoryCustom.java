package kr.hhplus.be.server.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TokenRepositoryCustom
 * - 커스텀 로직을 정의하는 인터페이스.
 */
public interface TokenRepositoryCustom {
    boolean existsActiveTokenByUserId(Long userId);
    void updateStatusAndExpiresAt(String tokenId, String status, LocalDateTime expiresAt);
    void updateStatusAndExpiresAtForWaitingTokens(String newStatus, LocalDateTime expiresAt);
    void updateTokenStatus(String tokenId, String status);
    List<String> findTokensByStatusWaiting(int limit);

}
