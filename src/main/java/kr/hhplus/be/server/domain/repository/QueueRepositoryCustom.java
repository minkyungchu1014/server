package kr.hhplus.be.server.domain.repository;

/**
 * QueueRepositoryCustom
 * - 커스텀 쿼리를 정의하기 위한 인터페이스.
 */
public interface QueueRepositoryCustom {

    /**
     * 특정 토큰의 대기열 순서를 반환.
     * @param tokenId 토큰 ID
     * @return 대기열 순서 (앞에 있는 WAITING 상태의 항목 수)
     */
    long getQueueOrderByToken(String tokenId);

    int countTokensByStatus(String status);
}
