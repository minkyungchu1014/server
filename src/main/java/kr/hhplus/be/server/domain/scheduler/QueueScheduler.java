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
     *  WAITING → ACTIVE로 변경 (1분마다 실행)
     */
    @Scheduled(fixedRate = 60000)
    public void assignQueueTokens() {
        int maxActiveTokens = 5;
        queueService.activateTokens(maxActiveTokens);
    }

    /**
     *  만료된 ACTIVE 토큰 삭제 (1분마다 실행)
     */
    @Scheduled(fixedRate = 60000)
    public void expireQueueTokens() {
        queueService.removeExpiredActiveTokens();
    }
}
