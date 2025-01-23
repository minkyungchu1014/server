package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.api.domain.dto.QueueStatusResponse;
import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.repository.QueueRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class QueueService {

    private final QueueRepository queueRepository;

    // 생성자 주입을 통해 queueRepository 제공받음
    public QueueService(QueueRepository queueRepository, PointRepository pointRepository) {
        this.queueRepository = queueRepository;
    }


    // 스케줄러 기반: 비활성화된 토큰 활성화
    @Transactional
    public void activateTokens(int maxActiveTokens) {
        // 현재 활성화된 토큰 수를 가져오기
        int currentActiveTokens = queueRepository.countTokensByStatus("ACTIVE");

        // 활성화해야 할 토큰 수 계산
        int tokensToActivate = maxActiveTokens - currentActiveTokens;

        // 활성화해야 할 토큰 수가 없으면 종료
        if (tokensToActivate <= 0) {
            System.out.println("No tokens to activate. Current active tokens: " + currentActiveTokens);
            return;
        }

        // WAITING 상태의 토큰 원하는 수만큼 가져오기
        List<String> inactiveTokens = queueRepository.findTokensByStatusWaiting((Pageable) PageRequest.of(0, maxActiveTokens));

        // 토큰 상태를 ACTIVE로 변경
        for (String token : inactiveTokens) {
            // WAITING 상태를 ACTIVE로 변경하고 만료 시간 설정
            queueRepository.updateStatusAndExpiresAtForWaitingTokens(token, "ACTIVE", LocalDateTime.now().plusMinutes(30));
        }
    }


    /**
     * 대기열 상태를 반환합니다.
     * @param tokenId 토큰 ID
     * @return QueueStatusResponse
     */
    public QueueStatusResponse getQueueStatusByToken(String tokenId) {
        Optional<Queue> queueOptional = queueRepository.findByTokenId(tokenId);
        QueueStatusResponse response = new QueueStatusResponse();

        if (queueOptional.isPresent()) {
            Queue queue = queueOptional.get();
            response.setTokenId(queue.getTokenId());
            response.setStatus(queue.getStatus());
            response.setExpiresAt(queue.getExpiresAt());

            if ("WAITING".equals(queue.getStatus())) {
                long position = queueRepository.getQueuePosition(tokenId);
                response.setQueuePosition(position);
            } else {
                response.setQueuePosition(0L);
            }
        } else {
            response.setTokenId(tokenId);
            response.setStatus("NOT_IN_QUEUE");
            response.setQueuePosition(null);
        }

        return response;
    }

    /**
     * 만료된 대기열 항목 삭제
     */
    @Transactional
    public void deleteByExpiresAtBefore() {
        queueRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    /**
     * 토큰을 대기열에 추가합니다.
     * @param tokenId 토큰 ID
     */
    @Transactional
    public void addToQueue(String tokenId, Long userId) {
        if (queueRepository.existsByTokenId(tokenId)) {
            return; // 중복 방지
        }

        Queue queue = new Queue();
        queue.setTokenId(tokenId);
        queue.setStatus("WAITING");
        queue.setUserId(userId);
        queue.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        queueRepository.save(queue);
    }

    @Transactional
    public void updateQueueStatus(String token, LocalDateTime expiresAt) {
        queueRepository.updateExpiresAt(token, expiresAt);
    }


    public boolean isQueueActive(String token) {
        return queueRepository.existsByTokenIdAndStatus(token, "ACTIVE");
    }


}
