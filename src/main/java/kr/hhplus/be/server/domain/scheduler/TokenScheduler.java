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


    /** 토큰 테이블에서
     * 1분마다 만료된 토큰 삭제(토큰 보관 주기 : 1년)
     */
    @Scheduled(fixedRate = 60000) //
    public void deleteByExpiresAtBefore() {
        tokenService.deleteByExpiresAtBefore();
    }
}
