package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * QueueRepository
 * - 기본 JPA 메서드와 커스텀 로직을 포함하는 대기열 관련 데이터 접근 레포지토리.
 */
@Repository
public interface QueueRepository extends JpaRepository<Queue, Long>, QueueRepositoryCustom {

    /**
     * 토큰 ID로 대기열 항목 조회.
     * @param tokenId 토큰 ID
     * @return 해당 토큰 ID에 해당하는 대기열 항목
     */
    Optional<Queue> findByTokenId(String tokenId);

    /**
     * 만료 시간이 지난 대기열 항목 삭제.
     * @param now 현재 시간
     */
    void deleteByExpiresAtBefore(LocalDateTime now);

    /**
     * 현재 시간보다 만료된 토큰 리스트를 조회합니다.
     * @param currentTime 현재 시간
     * @return 만료된 토큰 리스트
     */
    List<Queue> findByExpiresAtBefore(LocalDateTime currentTime);

    /**
     * 만료된 토큰 조회
     * @param status 토큰 상태 (ACTIVE)
     * @param currentTime 현재 시간
     * @return 만료된 토큰 ID 리스트
     */
    List<Queue> findByStatusAndExpiresAtBefore(String status, LocalDateTime currentTime);

    /**
     * 토큰 ID와 상태 값으로 대기열 항목 존재 여부 확인
     * @param tokenId 토큰 ID
     * @param status 상태 값
     * @return 존재 여부
     */
    boolean existsByTokenIdAndStatus(String tokenId, String status);

    boolean existsByTokenId(String tokenId);

    /**
     * 사용자 ID로 대기열 삭제
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);

    /**
     * 사용자 ID로 대기열 항목 수 조회
     * @param userId 사용자 ID
     * @return 대기열 항목 수
     */
    long countByUserId(Long userId);
    @Query("SELECT q.tokenId FROM Queue q WHERE q.status = 'WAITING' ORDER BY q.createdAt ASC")
    List<String> findTokensByStatusWaiting(Pageable pageable);

    @Modifying
    @Query("UPDATE Queue q SET q.status = :newStatus, q.expiresAt = :expiresAt WHERE q.tokenId = :tokenId")
    void updateStatusAndExpiresAtForWaitingTokens(@Param("tokenId") String tokenId,
                                                  @Param("newStatus") String newStatus,
                                                  @Param("expiresAt") LocalDateTime expiresAt);
}


