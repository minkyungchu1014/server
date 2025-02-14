package kr.hhplus.be.server.domain.scheduler;

import kr.hhplus.be.server.domain.service.TokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenScheduler {

    private final TokenService tokenService;

    public TokenScheduler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * 토큰 테이블에서 1시간마다 만료된 토큰 삭제
     */
    @Scheduled(fixedRate = 3600000) // 1시간(3,600,000ms)마다 실행
    public void deleteByExpiresAtBefore() {
        tokenService.deleteByExpiresAtBefore();
    }
}
