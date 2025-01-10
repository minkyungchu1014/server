package kr.hhplus.be.server.domain.scheduler;

import kr.hhplus.be.server.domain.service.ExpirationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiredReservationScheduler {

    private final ExpirationService expirationService;

    public ExpiredReservationScheduler(ExpirationService expirationService) {
        this.expirationService = expirationService;
    }

    /**
     * 매 1분마다 만료된 예약과 토큰을 처리합니다.
     */
    @Scheduled(fixedRate = 60000)
    public void processExpiredReservationsAndTokens() {
        expirationService.processExpiredReservationsAndTokens();
    }
}
