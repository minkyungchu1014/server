package kr.hhplus.be.server.domain.scheduler;

import kr.hhplus.be.server.domain.service.QueueService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueScheduler {

    private final QueueService queueService;

    public QueueScheduler(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * 배정 스케줄러: WAITING 상태의 토큰을 ACTIVE로 변경
     * maxActiveTokens : 5
     * 매 1분마다 실행
     */
    @Scheduled(fixedRate = 60000) // 1분
    public void assignQueueTokens() {
        int maxActiveTokens = 5;
        queueService.activateTokens(maxActiveTokens);
    }

    /**
     * 만료 스케줄러: 대기열(queue)에서 ACTIVE(30분) 등
     * 오래된 WAITING 상태(1시간) 만료 처리
     * 매 1분마다 실행
     */
    @Scheduled(fixedRate = 60000) // 1분
    public void expireQueueTokens() {
        queueService.deleteByExpiresAtBefore();
    }
}