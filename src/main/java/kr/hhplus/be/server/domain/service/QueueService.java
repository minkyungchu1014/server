package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.repository.QueueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QueueService {

    private final QueueRepository queueRepository;

    // 생성자 주입을 통해 queueRepository 제공받음
    public QueueService(QueueRepository queueRepository, PointRepository pointRepository) {
        this.queueRepository = queueRepository;
    }

    /**
     * 특정 토큰의 대기열 순서를 반환.
     * @param tokenId 토큰 ID
     * @return 대기열 순서
     */
    public long getQueueOrderByToken(String tokenId) {
        return queueRepository.getQueueOrderByToken(tokenId);
    }

    /**
     * 새로운 대기열 항목 추가.
     * @param tokenId 토큰 ID
     */
    public void addQueueEntry(String tokenId) {
        Queue queue = new Queue();
        queue.setTokenId(tokenId);
        queue.setStatus("WAITING");
        queue.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        queue.setCreatedAt(LocalDateTime.now());
        queueRepository.save(queue);
    }

    /**
     * 만료된 대기열 항목 삭제.
     */
    public void removeExpiredEntries() {
        queueRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    /**
     * 특정 토큰 ID로 대기열 항목 조회.
     * @param tokenId 토큰 ID
     * @return 대기열 항목 (Optional)
     */
    public Optional<Queue> findQueueEntryByTokenId(String tokenId) {
        return queueRepository.findByTokenId(tokenId);
    }

    public boolean isQueueActive(String token) {
        return queueRepository.existsByTokenIdAndStatus(token, "ACTIVE");
    }
}
