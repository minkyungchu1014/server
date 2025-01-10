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
     * 1분마다 WAITING 상태의 토큰을 ACTIVE로 변경 (만료 시간 10분 후로 설정)
     */
    @Scheduled(fixedRate = 60000) // 매 1분 실행
    public void activateTokens() {
        tokenService.activateTokens(10);
    }

    /**c
     * 1분마다 만료된 토큰 삭제
     */
    @Scheduled(fixedRate = 60000) // 매 1분 실행
    public void cleanExpiredTokens() {
        tokenService.expireTokens();
    }
}
