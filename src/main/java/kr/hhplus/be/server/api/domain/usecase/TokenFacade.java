package kr.hhplus.be.server.api.domain.usecase;

import kr.hhplus.be.server.domain.service.QueueService;
import kr.hhplus.be.server.domain.service.TokenService;
import org.springframework.stereotype.Component;

/**
 * TokenFacade
 * - 토큰 및 대기열과 관련된 고수준 비즈니스 로직을 처리하는 클래스.
 * - Service 계층을 조합하여 클라이언트가 직접 호출하는 컨트롤러와 상호작용.
 */
@Component
public class TokenFacade {

    private final TokenService tokenService;
    private final QueueService queueService;

    public TokenFacade(TokenService tokenService, QueueService queueService) {
        this.tokenService = tokenService;
        this.queueService = queueService;
    }

    /**
     * 사용자에게 새 토큰을 생성합니다.
     * @param userId 토큰을 생성할 사용자 ID
     * @return 생성된 토큰 값
     */
    public String generateToken(Long userId) {
        String token = tokenService.generateToken(userId); // 토큰 생성 및 대기열 추가
        queueService.addToQueue(token, userId);
        return token;
    }
}
